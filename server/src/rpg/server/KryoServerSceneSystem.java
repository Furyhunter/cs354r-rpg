package rpg.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import rpg.scene.Node;
import rpg.scene.components.Component;
import rpg.scene.components.RectangleRenderer;
import rpg.scene.components.SimplePlayerComponent;
import rpg.scene.kryo.*;
import rpg.scene.replication.*;
import rpg.scene.systems.NetworkingSceneSystem;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class KryoServerSceneSystem extends NetworkingSceneSystem {

    private Server server;
    private ServerListener listener = new ServerListener();

    private Map<Integer, Node> nodeMap = new TreeMap<>();
    private Map<Integer, Component> componentMap = new TreeMap<>();

    private Set<Node> nodesToReattach = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
    private Set<Component> componentsToAttach = new TreeSet<>(Comparator.comparingInt(Component::getNetworkID));
    private Set<Component> componentsToReattach = new TreeSet<>(Comparator.comparingInt(Component::getNetworkID));
    private Set<Component> componentsToDetach = new TreeSet<>(Comparator.comparingInt(Component::getNetworkID));

    private RelevantSetDecider relevantSetDecider = new AlwaysRelevantDecider();

    private List<Player> players = new ArrayList<>();
    private Map<Connection, Player> connectionPlayerMap = new TreeMap<>(Comparator.comparingInt(Connection::getID));

    private Map<Player, Set<Node>> oldRelevantSets = new TreeMap<>(Comparator.comparingInt(p -> p.kryoConnection.getID()));

    private float timeBuffer = 0;
    private float replicationRate = 5;
    private int currentTick = 0;

    private Map<Integer, FieldReplicateMessage> oldReplicationState = new TreeMap<>();

    private List<RPCMessage> multicastRPCs = new ArrayList<>();
    private List<RPCMessage> clientRPCs = new ArrayList<>();

    class ServerListener extends Listener {
        @Override
        public void connected(Connection connection) {
            Node playerNode = new Node(getParent().getRoot());
            playerNode.addComponent(new RectangleRenderer());
            playerNode.addComponent(new SimplePlayerComponent());
            Player player = new Player(playerNode, connection);
            players.add(player);
            connectionPlayerMap.put(connection, player);
            playerNode.getTransform().sendRPC("possessNode");

            oldRelevantSets.put(player, new TreeSet<>(Comparator.comparingInt(Node::getNetworkID)));
            // when next we send a tick, we'll get a "new" relevant set

            Log.info(getClass().getSimpleName(), "Player "
                    + player.kryoConnection.getID()
                    + " at " + player.kryoConnection.getRemoteAddressTCP().toString()
                    + " connected.");
        }

        @Override
        public void received(Connection connection, Object o) {
            if (o instanceof RPCMessage) {
                // We can handle it immediately since server update is synchronous with app thread.
                RPCMessage rpcMessage = (RPCMessage) o;
                Player p = connectionPlayerMap.get(connection);

                Component target = componentMap.get(rpcMessage.targetNetworkID);
                if (target == null) {
                    // Disconnect for security failure.
                    Log.warn(getClass().getSimpleName(),
                            "Player " + connection.getID()
                                    + " is being kicked for trying to execute an RPC on a component that doesn't exist.");
                    connection.close();
                    return;
                }
                if (target.getParent().getNetworkID() != p.possessedNode.getNetworkID()) {
                    Log.warn(getClass().getSimpleName(),
                            "Player " + connection.getID() + " is being kicked for trying to execute an RPC" +
                                    " on a component not belonging to one of its nodes.");
                    connection.close();
                }

                RepTable t = RepTable.getTableForType(target.getClass());
                Method m = t.getRPCMethod(rpcMessage.invocation.methodId);
                try {
                    m.setAccessible(true);
                    m.invoke(target, rpcMessage.invocation.arguments.toArray());
                } catch (Exception e) {
                    Log.error(getClass().getSimpleName(), "Player " + connection.getID()
                            + " is being kicked because of the following RPC exception", e);
                }
            }
        }

        @Override
        public void disconnected(Connection connection) {
            Player p = connectionPlayerMap.remove(connection);
            players.remove(p);
            oldRelevantSets.remove(p);

            p.possessedNode.getParent().removeChild(p.possessedNode);

            Log.info(getClass().getSimpleName(), "Player "
                    + p.kryoConnection.getID() + " disconnected.");

        }
    }

    public KryoServerSceneSystem() throws IOException {
        server = new Server();
        KryoClassRegisterUtil.registerAll(server.getKryo()); // register all known classes for serialization
        server.addListener(listener);
        server.bind(31425, 31426);

        RepTableInitializeUtil.initializeRepTables();
    }

    @Override
    public void processNode(Node n, float deltaTime) {

    }

    @Override
    public void nodeReattached(Node n) {
        nodesToReattach.add(n);
    }

    @Override
    public void componentAttached(Component c) {
        componentMap.put(c.getNetworkID(), c);
        componentsToAttach.add(c);
        componentsToDetach.remove(c);
    }

    @Override
    public void componentDetached(Component c) {
        componentMap.remove(c.getNetworkID());
        componentsToDetach.add(c);
        componentsToAttach.remove(c);
    }

    @Override
    public void componentReattached(Component c) {
        componentsToReattach.add(c);
    }

    @Override
    public void beginProcessing() {
        try {
            server.update(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getContext() {
        return Context.Server;
    }

    @Override
    public void addRPCMessage(RPCMessage m) {
        Objects.requireNonNull(m);
        clientRPCs.add(m);
    }

    @Override
    public void addMulticastRPCMessage(RPCMessage m) {
        Objects.requireNonNull(m);
        multicastRPCs.add(m);
    }

    @Override
    public float getTickDeltaTime() {
        return 0;
    }

    @Override
    public void endProcessing() {
        try {
            server.update(0);
        } catch (IOException e) {
            Log.error(getClass().getSimpleName(), e);
        }

        float deltaTime = Gdx.graphics.getDeltaTime();
        timeBuffer += deltaTime;

        if (timeBuffer > (1f / replicationRate)) {

            Map<Integer, FieldReplicateMessage> newReplicationState = new TreeMap<>();

            // First, we need to get all the new component replication states.
            componentMap.values().forEach(c -> {
                RepTable t = RepTable.getTableForType(c.getClass());
                FieldReplicationData frd = t.replicateFull(c);
                FieldReplicateMessage frm = new FieldReplicateMessage(c.getNetworkID(), frd);
                newReplicationState.put(c.getNetworkID(), frm);
            });

            /* During this processing step, the scene is considered "immutable", so we can
             * process the scene in parallel for all connected clients. Below is the general
             * process of handling the replication tick for each player:
             *
             * 1. Send BeginTick to indicate a block of information for a tick.
             * 2. Get the new relevant set.
             * 3. Ensure that each node in the relevant set's parent is in the relevant set.
             * 4. Compare the old relevant set and new relevant set to get the delta relevance. This yields us 3 sets:
             *    new relevancy (attach), new irrelevancy (detach), and consistently relevant between ticks.
             * 4. For consistently relevant nodes, we must figure out if their parent has changed since the last tick.
             *    If it has, then we have to reattach -- and if the new parent is not relevant, then that node can't
             *    be relevant, so we'll go ahead and send a detach event for that node.
             * 5. Replicate components.
             * 6. EndTick
             */
            players.parallelStream().forEach(p -> {
                // Send begin tick.
                BeginTick bt = new BeginTick();
                bt.tickID = currentTick;
                p.kryoConnection.sendTCP(bt);

                // Get the relevant set
                Set<Node> newRelevantSet = relevantSetDecider.getRelevantSetForNode(getParent(), p.possessedNode);
                Set<Node> oldRelevantSet = oldRelevantSets.get(p);
                if (oldRelevantSet == null) {
                    oldRelevantSet = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
                }

                // Remove nodes whose parents aren't relevant from relevant set.
                newRelevantSet.removeIf(
                        n -> n.getParent() == null
                                || (n.getParent().getNetworkID() >= 0 && !newRelevantSet.contains(n.getParent())));

                // Remove nodes who aren't set to replicate.
                newRelevantSet.removeIf(n -> !n.isReplicated());

                // The relevant.
                Set<Node> newlyRelevant = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
                newlyRelevant.addAll(newRelevantSet);
                newlyRelevant.removeAll(oldRelevantSet);

                // The no longer relevant.
                Set<Node> newlyNonRelevant = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
                newlyNonRelevant.addAll(oldRelevantSet);
                newlyNonRelevant.removeAll(newRelevantSet);

                // The consistently relevant nodes between ticks.
                Set<Node> inBoth = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
                inBoth.addAll(newRelevantSet);
                inBoth.retainAll(oldRelevantSet);

                // Ensure that any reattached nodes get reattach messages IF AND ONLY IF their new parent is relevant.
                List<Node> notActuallyRelevant = inBoth.parallelStream()
                        .filter(nodesToReattach::contains)
                        .filter(n -> !newRelevantSet.contains(n))
                        .collect(Collectors.toList());

                newlyNonRelevant.addAll(notActuallyRelevant);
                inBoth.removeAll(notActuallyRelevant);

                // Nodes to send reattachment messages are all nodes consistently relevant that are also in the reattachment set.
                List<Node> nodesForReattachment = inBoth.parallelStream()
                        .filter(nodesToReattach::contains)
                        .collect(Collectors.toList());

                // Reattach nodes that need to be sent reattachments.
                nodesForReattachment.forEach(n -> {
                    NodeReattach nodeReattach = new NodeReattach();
                    nodeReattach.nodeID = n.getNetworkID();
                    nodeReattach.parentID = n.getParent().getNetworkID();
                    p.kryoConnection.sendTCP(nodeReattach);
                });


                // For a player that has yet to receive a tick,
                // newlyRelevant == newRelevantSet (everything)
                // newlyIrrelevant == oldRelevantSet (nothing)
                newlyRelevant.forEach(n -> {
                    NodeAttach nodeAttach = new NodeAttach();
                    nodeAttach.nodeID = n.getNetworkID();
                    nodeAttach.parentID = n.getParent().getNetworkID();
                    p.kryoConnection.sendTCP(nodeAttach);
                });

                // Now, get all of the irrelevant nodes to detach.
                newlyNonRelevant.forEach(n -> {
                    NodeDetach nodeDetach = new NodeDetach();
                    nodeDetach.nodeID = n.getNetworkID();
                    p.kryoConnection.sendTCP(nodeDetach);
                });

                /* Components!
                 * 1. All components of newly relevant nodes are newly relevant.
                 * 2. Components of newly irrelevant nodes are implicitly removed, so no message is sent.
                 * 3. Components attached, detached, reattached whose parents are consistently relevant
                 *    will have messages sent for them.
                 */

                Set<Component> newlyRelevantComponents = new TreeSet<>(Comparator.comparingInt(Component::getNetworkID));
                Set<Component> newlyIrrelevantComponents = new TreeSet<>(Comparator.comparingInt(Component::getNetworkID));
                Set<Component> reattachedComponents = new TreeSet<>(Comparator.comparingInt(Component::getNetworkID));
                Set<Component> consistantlyRelevantComponents = new TreeSet<>(Comparator.comparingInt(Component::getNetworkID));

                newlyRelevant.stream().filter(n -> n.getParent() != null).forEach(n -> newlyRelevantComponents.addAll(n.getComponents()));

                inBoth.forEach(n -> {
                    n.getComponents().forEach(consistantlyRelevantComponents::add);
                });

                componentMap.values().stream()
                        .filter(c -> inBoth.contains(c.getParent()))
                        .filter(c -> !componentsToDetach.contains(c))
                        .forEach(consistantlyRelevantComponents::add);

                componentsToAttach.stream()
                        .filter(c -> c.getParent().getNetworkID() >= 0 && inBoth.contains(c.getParent()))
                        .forEach(newlyRelevantComponents::add);
                componentsToDetach.stream()
                        .filter(c -> c.getParent().getNetworkID() >= 0 && inBoth.contains(c.getParent()))
                        .forEach(newlyIrrelevantComponents::add);
                componentsToReattach.stream()
                        .filter(c -> c.getParent().getNetworkID() >= 0 && inBoth.contains(c.getParent()))
                        .forEach(reattachedComponents::add);

                newlyRelevantComponents.forEach(c -> {
                    ComponentAttach ca = new ComponentAttach();
                    ca.componentID = c.getNetworkID();
                    ca.parentNodeID = c.getParent().getNetworkID();
                    ca.repClassID = RepTable.getClassIDForType(c.getClass());
                    p.kryoConnection.sendTCP(ca);
                });

                newlyIrrelevantComponents.forEach(c -> {
                    ComponentDetach cd = new ComponentDetach();
                    cd.componentID = c.getNetworkID();
                    p.kryoConnection.sendTCP(cd);
                });

                reattachedComponents.forEach(c -> {
                    ComponentReattach cr = new ComponentReattach();
                    cr.componentID = c.getNetworkID();
                    cr.parentNodeID = c.getParent().getNetworkID();
                    p.kryoConnection.sendTCP(cr);
                });

                oldRelevantSets.put(p, newRelevantSet);


                /* ACTUAL REPLICATION */

                // Field replication

                // Newly relevant components get entire field replication state.
                newlyRelevantComponents.forEach(c -> {
                    FieldReplicateMessage m = newReplicationState.get(c.getNetworkID());
                    if (m.fieldReplicationData.fieldData.size() == 0) return;
                    p.kryoConnection.sendTCP(m);
                });

                // Components belonging to nodes in consistent relevancy should get delta field replications
                consistantlyRelevantComponents.forEach(c -> {
                    FieldReplicateMessage oldFRM = oldReplicationState.get(c.getNetworkID());
                    if (oldFRM == null) {
                        // send entire new replication state since we have no old state...
                        FieldReplicateMessage m = newReplicationState.get(c.getNetworkID());
                        p.kryoConnection.sendTCP(m);
                        return;
                    }
                    FieldReplicationData oldFRD = oldFRM.fieldReplicationData;
                    FieldReplicationData newFRD = newReplicationState.get(c.getNetworkID()).fieldReplicationData;


                    FieldReplicateMessage m = new FieldReplicateMessage();
                    m.fieldReplicationData = oldFRD.diff(newFRD);
                    if (m.fieldReplicationData.fieldData.size() == 0 && !c.isAlwaysFieldReplicated()) {
                        // nothing has changed. send nothing for optimization.
                        return;
                    }
                    m.componentID = c.getNetworkID();
                    int sent = p.kryoConnection.sendTCP(m);
                });


                // Multicast RPCs (only if component is in relevant set)
                multicastRPCs.stream().filter(r -> {
                    Component c = componentMap.get(r.targetNetworkID);
                    return c != null && newRelevantSet.contains(c.getParent());
                }).forEach(p.kryoConnection::sendTCP);

                // Client RPCS
                // These should only be sent to the possessing client.
                clientRPCs.stream().filter(r -> {
                    Component c = componentMap.get(r.targetNetworkID);
                    return c != null && c.getParent() == p.possessedNode && newRelevantSet.contains(c.getParent());
                }).forEach(p.kryoConnection::sendTCP);


                EndTick endTick = new EndTick();
                endTick.tickID = currentTick;
                p.kryoConnection.sendTCP(endTick);
            });

            timeBuffer = 0;

            currentTick++;
            // We set to 0 because we don't want to end up sending
            // a whole bunch of ticks all at once if there's a long GC pause.

            nodesToReattach.clear();

            componentsToAttach.clear();
            componentsToDetach.clear();
            componentsToReattach.clear();

            clientRPCs.clear();
            multicastRPCs.clear();

            oldReplicationState = newReplicationState;
        }
    }

    public float getReplicationRate() {
        return replicationRate;
    }

    public void setReplicationRate(float replicationRate) {
        this.replicationRate = replicationRate;
    }

    @Override
    public boolean doesProcessNodes() {
        return false;
    }
}
