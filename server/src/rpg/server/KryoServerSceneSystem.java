package rpg.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import rpg.scene.Node;
import rpg.scene.components.Component;
import rpg.scene.kryo.ComponentAttach;
import rpg.scene.kryo.NodeAttach;
import rpg.scene.replication.RepTable;
import rpg.scene.systems.AbstractSceneSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    private float timeBuffer = 0;

    private float replicationRate = 10;

    class ServerListener extends Listener {
        @Override
        public void connected(Connection connection) {
            // Send all nodes
            // TODO relevant set in endProcessing instead

            for (Node n : nodeMap.values()) {
                NodeAttach msg = new NodeAttach();
                msg.nodeID = n.getNetworkID();
                msg.parentID = n.getParent().getNetworkID();
                connection.sendTCP(msg);
            }

            for (Component c : componentMap.values()) {
                ComponentAttach msg = new ComponentAttach();
                msg.componentID = c.getNetworkID();
                msg.parentNodeID = c.getParent().getNetworkID();
                msg.repClassID = RepTable.getClassIDForType(c.getClass());
                connection.sendTCP(msg);
            }
        }

        @Override
        public void received(Connection connection, Object o) {
            super.received(connection, o);
        }

        @Override
        public void disconnected(Connection connection) {
            super.disconnected(connection);
        }
    }

    public KryoServerSceneSystem() throws IOException {
        server = new Server();
        server.addListener(listener);
        server.bind(12523, 12524);
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
            // Node attachment

            timeBuffer = 0;
        }

    }

    public float getReplicationRate() {
        return replicationRate;
    }

    public void setReplicationRate(float replicationRate) {
        this.replicationRate = replicationRate;
    }
}
