package rpg.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import rpg.scene.Node;
import rpg.scene.components.Component;
import rpg.scene.kryo.*;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPCMessage;
import rpg.scene.systems.NetworkingSceneSystem;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class KryoServerSceneSystem extends NetworkingSceneSystem {

    private Server server;
    private ServerListener listener = new ServerListener();

    private Map<Integer, Node> nodeMap = new TreeMap<>();
    private Map<Integer, Component> componentMap = new TreeMap<>();

    private Set<Node> nodesToReattach = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));

    private List<Component> componentsToAttach = new ArrayList<>();
    private List<Component> componentsToDetach = new ArrayList<>();
    private List<Component> componentsToReattach = new ArrayList<>();

    private RelevantSetDecider relevantSetDecider = new AlwaysRelevantDecider();

    private List<Player> players = new ArrayList<>();
    private Map<Connection, Player> connectionPlayerMap = new TreeMap<>(Comparator.comparingInt(Connection::getID));

    private Map<Player, Set<Node>> oldRelevantSets = new TreeMap<>(Comparator.comparingInt(p -> p.kryoConnection.getID()));

    private float timeBuffer = 0;
    private float replicationRate = 10;
    private int currentTick = 0;

    class ServerListener extends Listener {
        @Override
        public void connected(Connection connection) {
            Node playerNode = new Node(getParent().getRoot());
            Player player = new Player(playerNode, connection);
            players.add(player);
            connectionPlayerMap.put(connection, player);

            oldRelevantSets.put(player, new TreeSet<>(Comparator.comparingInt(Node::getNetworkID)));
            // when next we send a tick, we'll get a "new" relevant set
        }

        @Override
        public void received(Connection connection, Object o) {
        }

        @Override
        public void disconnected(Connection connection) {
            Player p = connectionPlayerMap.remove(connection);
            players.remove(p);
            oldRelevantSets.remove(p);

            p.possessedNode.getParent().removeChild(p.possessedNode);

        }
    }

    public KryoServerSceneSystem() throws IOException {
        server = new Server();
        KryoClassRegisterUtil.registerAll(server.getKryo()); // register all known classes for serialization
        server.addListener(listener);
        server.bind(31425, 31426);
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
    }

    @Override
    public void componentDetached(Component c) {
        componentsToDetach.add(c);
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
    public void processRPC(RPCMessage m) {
        // do nothing, we'll handle this otherwise
    }

    @Override
    public void processMulticastRPC(RPCMessage m) {
        // do nothing, we'll handle this otherwise
    }

    @Override
    public boolean canProcessRPCs() {
        // we aren't calling super.endProcessing
        return true;
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

                // Remove nodes whose parents aren't relevant from relevant set.
                List<Node> notActuallyRelevant = newRelevantSet.parallelStream()
                        .filter(n -> (n.getParent() == null || !newRelevantSet.contains(n.getParent())))
                        .collect(Collectors.toList());
                newRelevantSet.removeAll(notActuallyRelevant);

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
                notActuallyRelevant = inBoth.parallelStream()
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
                    if (n.getNetworkID() > 0) {
                        NodeReattach nodeReattach = new NodeReattach();
                        nodeReattach.nodeID = n.getNetworkID();
                        nodeReattach.parentID = n.getParent().getNetworkID();
                        p.kryoConnection.sendTCP(nodeReattach);
                    }
                });


                // For a player that has yet to receive a tick,
                // newlyRelevant == newRelevantSet (everything)
                // newlyIrrelevant == oldRelevantSet (nothing)
                newlyRelevant.forEach(n -> {
                    if (n.getNetworkID() > 0) {
                        NodeAttach nodeAttach = new NodeAttach();
                        nodeAttach.nodeID = n.getNetworkID();
                        nodeAttach.parentID = n.getParent().getNetworkID();
                        p.kryoConnection.sendTCP(nodeAttach);
                    }
                });

                // Now, get all of the irrelevant nodes to detach.
                newlyNonRelevant.forEach(n -> {
                    if (n.getNetworkID() > 0) {
                        NodeDetach nodeDetach = new NodeDetach();
                        nodeDetach.nodeID = n.getNetworkID();
                        p.kryoConnection.sendTCP(nodeDetach);
                    }
                });

                // Now, again for all of the now-relevant nodes, create and replicate all components.

                EndTick endTick = new EndTick();
                endTick.tickID = currentTick;
                p.kryoConnection.sendTCP(endTick);

                oldRelevantSets.put(p, newRelevantSet);
            });

            timeBuffer = 0;

            currentTick++;
            // We set to 0 because we don't want to end up sending
            // a whole bunch of ticks all at once if there's a long GC pause.

            nodesToReattach.clear();
        }
    }

    public float getReplicationRate() {
        return replicationRate;
    }

    public void setReplicationRate(float replicationRate) {
        this.replicationRate = replicationRate;
    }
}
