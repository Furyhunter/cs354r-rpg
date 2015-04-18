package rpg.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import rpg.scene.Node;
import rpg.scene.components.Component;
import rpg.scene.kryo.*;
import rpg.scene.systems.AbstractSceneSystem;

import java.io.IOException;
import java.util.*;

public class KryoServerSceneSystem extends AbstractSceneSystem {

    private Server server;
    private ServerListener listener = new ServerListener();

    private Map<Integer, Node> nodeMap = new TreeMap<>();
    private Map<Integer, Component> componentMap = new TreeMap<>();

    private List<Node> nodesToAttach = new ArrayList<>();
    private List<Node> nodesToDetach = new ArrayList<>();
    private List<Node> nodesToReattach = new ArrayList<>();

    private List<Component> componentsToAttach = new ArrayList<>();
    private List<Component> componentsToDetach = new ArrayList<>();
    private List<Component> componentsToReattach = new ArrayList<>();

    private RelevantSetDecider relevantSetDecider = new AlwaysRelevantDecider();

    private List<Player> players = new ArrayList<>();
    private Map<Connection, Player> connectionPlayerMap;

    private Map<Player, List<Node>> oldRelevantSets;
    private Map<Player, List<Node>> newRelevantSets;

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

            oldRelevantSets.put(player, new ArrayList<>());
            // when next we send a tick, we'll get a "new" relevant set
        }

        @Override
        public void received(Connection connection, Object o) {
        }

        @Override
        public void disconnected(Connection connection) {
            players.remove(connectionPlayerMap.remove(connection));
        }
    }

    public KryoServerSceneSystem() throws IOException {
        server = new Server();
        KryoClassRegisterUtil.registerAll(server.getKryo()); // register all known classes for serialization
        server.addListener(listener);
        server.bind(12523, 12524);

        connectionPlayerMap = new TreeMap<>(Comparator.comparingInt(Connection::getID));

        oldRelevantSets = new TreeMap<>(Comparator.comparingInt(p -> p.kryoConnection.getID()));
        newRelevantSets = new TreeMap<>(Comparator.comparingInt(p -> p.kryoConnection.getID()));
    }

    @Override
    public void processNode(Node n, float deltaTime) {

    }

    @Override
    public void nodeAttached(Node n) {
        nodeMap.put(n.getNetworkID(), n);
        nodesToAttach.add(n);
    }

    @Override
    public void nodeDetached(Node n) {
        nodesToDetach.add(n);
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
    public void endProcessing() {
        float deltaTime = Gdx.graphics.getRawDeltaTime();
        timeBuffer += deltaTime;

        if (timeBuffer > (1f / replicationRate)) {

            players.forEach(p -> {
                BeginTick bt = new BeginTick();
                bt.tickID = currentTick;
                p.kryoConnection.sendTCP(bt);

                // New relevant set
                List<Node> newRelevantSet = relevantSetDecider.getRelevantSetForNode(getParent(), p.possessedNode);
                List<Node> oldRelevantSet = oldRelevantSets.get(p);

                // All nodes which were not in oldRelevantSet and are in newRelevantSet
                List<Node> newlyRelevant = new ArrayList<>(newRelevantSet);
                newlyRelevant.removeAll(oldRelevantSet);

                // All nodes which were not in newRelevantSet and are in oldRelevantSet
                List<Node> newlyNonRelevant = new ArrayList<>(oldRelevantSet);
                newlyNonRelevant.removeAll(newRelevantSet);

                // For a player that has yet to receive a tick,
                // newlyRelevant == newRelevantSet (everything)
                // newlyIrrelevant == oldRelevantSet (nothing)
                newRelevantSet.forEach(n -> {
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

                EndTick endTick = new EndTick();
                endTick.tickID = currentTick;

                currentTick++;
            });

            timeBuffer = 0;
            // We set to 0 because we don't want to end up sending
            // a whole bunch of ticks all at once if there's a long GC pause.
        }


    }

    public float getReplicationRate() {
        return replicationRate;
    }

    public void setReplicationRate(float replicationRate) {
        this.replicationRate = replicationRate;
    }
}
