package rpg.client;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import rpg.scene.Node;
import rpg.scene.components.Component;
import rpg.scene.components.Transform;
import rpg.scene.kryo.*;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPCMessage;
import rpg.scene.replication.RepTable;
import rpg.scene.replication.RepTableInitializeUtil;
import rpg.scene.systems.NetworkingSceneSystem;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;

public class KryoClientSceneSystem extends NetworkingSceneSystem {

    private Client client;
    private InetAddress hostAddress;

    @Override
    public void nodeAttached(Node n) {
        nodeMap.put(n.getNetworkID(), n);
    }

    @Override
    public void nodeDetached(Node n) {
        nodeMap.remove(n.getNetworkID());
    }

    @Override
    public void componentAttached(Component c) {
        componentMap.put(c.getNetworkID(), c);
    }

    @Override
    public void componentDetached(Component c) {
        componentMap.remove(c.getNetworkID());
    }

    private boolean connecting = false;

    private final List<Object> objectsFromServer = new ArrayList<>();

    private List<RPCMessage> rpcsToSend = new ArrayList<>();
    private boolean newTickAvailable = false;

    private Map<Integer, Node> nodeMap = new TreeMap<>();
    private Map<Integer, Component> componentMap = new TreeMap<>();

    private float lastTickTime = 0;
    private float tickDeltaTime = 0;
    private float time = 0;

    public void setHostAddress(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    class ClientListener extends Listener {

        int currentTick = 0;
        boolean processingTick = false;

        List<Object> tickObjectBuffer = new ArrayList<>();

        @Override
        public void connected(Connection connection) {
            Log.info(getClass().getSimpleName(), "Connected");
        }

        @Override
        public void disconnected(Connection connection) {
            Log.info(getClass().getSimpleName(), "Disconnected");
        }

        @Override
        public void received(Connection connection, Object o) {
            if (o instanceof BeginTick) {
                if (processingTick) {
                    Log.error(getClass().getSimpleName(), "The server sent a BeginTick before ending the last tick.");
                    return;
                }
                BeginTick t = (BeginTick) o;
                processingTick = true;
                currentTick = t.tickID;
                tickObjectBuffer.clear();
                return;
            }
            if (o instanceof EndTick) {
                EndTick t = (EndTick) o;
                if (!processingTick) {
                    Log.error(getClass().getSimpleName(), "The server sent an EndTick before starting a tick.");
                    return;
                }

                if (currentTick != t.tickID) {
                    Log.error(getClass().getSimpleName(), "The server sent an EndTick not corresponding to the last tick started.");
                    processingTick = false;
                    tickObjectBuffer.clear();
                    return;
                }
                synchronized (objectsFromServer) {
                    objectsFromServer.clear();
                    objectsFromServer.addAll(tickObjectBuffer);
                    newTickAvailable = true;
                }
                processingTick = false;
                return;
            }

            if (processingTick) {
                tickObjectBuffer.add(o);
            }
        }
    }

    public KryoClientSceneSystem() throws IOException {
        client = new Client();
        KryoClassRegisterUtil.registerAll(client.getKryo());
        client.addListener(new ClientListener());

        RepTableInitializeUtil.initializeRepTables();
    }

    @Override
    public Context getContext() {
        return Context.Client;
    }

    @Override
    public void addRPCMessage(RPCMessage m) {
        Objects.requireNonNull(m);
        rpcsToSend.add(m);
    }

    @Override
    public void addMulticastRPCMessage(RPCMessage m) {
        throw new RuntimeException("The client can never send multicast RPCs. Something went wrong.");
    }

    @Override
    public float getTickDeltaTime() {
        return tickDeltaTime - lastTickTime;
    }

    @Override
    public void processNode(Node n, float deltaTime) {

    }

    @Override
    public void beginProcessing() {
        time += Gdx.graphics.getRawDeltaTime();
        if (!connecting) {
            client.start();
            new Thread(() -> {
                try {
                    client.connect(5000, hostAddress, 31425, 31426);
                } catch (IOException e) {
                    Log.error(KryoClientSceneSystem.class.getSimpleName(), e);
                }
            }).start();
            connecting = true;
        }

        if (client.isConnected()) {
            if (newTickAvailable) {
                // Set tick delta times.
                lastTickTime = tickDeltaTime;
                tickDeltaTime = time;

                List<NodeAttach> nodeAttachList = new ArrayList<>();
                List<NodeDetach> nodeDetachList = new ArrayList<>();
                List<NodeReattach> nodeReattachList = new ArrayList<>();
                List<ComponentAttach> componentAttachList = new ArrayList<>();
                List<ComponentDetach> componentDetachList = new ArrayList<>();
                List<ComponentReattach> componentReattachList = new ArrayList<>();
                List<FieldReplicateMessage> fieldReplicateMessageList = new ArrayList<>();
                List<RPCMessage> rpcMessageList = new ArrayList<>();

                synchronized (objectsFromServer) {
                    objectsFromServer.stream().filter(o -> o instanceof NodeAttach).forEach(o -> nodeAttachList.add((NodeAttach) o));
                    objectsFromServer.stream().filter(o -> o instanceof NodeDetach).forEach(o -> nodeDetachList.add((NodeDetach) o));
                    objectsFromServer.stream().filter(o -> o instanceof NodeReattach).forEach(o -> nodeReattachList.add((NodeReattach) o));
                    objectsFromServer.stream().filter(o -> o instanceof ComponentAttach).forEach(o -> componentAttachList.add((ComponentAttach) o));
                    objectsFromServer.stream().filter(o -> o instanceof ComponentDetach).forEach(o -> componentDetachList.add((ComponentDetach) o));
                    objectsFromServer.stream().filter(o -> o instanceof ComponentReattach).forEach(o -> componentReattachList.add((ComponentReattach) o));
                    objectsFromServer.stream().filter(o -> o instanceof FieldReplicateMessage).forEach(o -> fieldReplicateMessageList.add((FieldReplicateMessage) o));
                    objectsFromServer.stream().filter(o -> o instanceof RPCMessage).forEach(o -> rpcMessageList.add((RPCMessage) o));
                    objectsFromServer.clear();
                }
                newTickAvailable = false;

                /* Nodes */

                // Attachment
                if (nodeAttachList.size() > 0) {
                    List<Node> nodesToAttach = new ArrayList<>();
                    List<Integer> parents = new ArrayList<>();
                    nodeAttachList.forEach(m -> {
                        Node node = new Node(m.nodeID, false);
                        nodeMap.put(m.nodeID, node);

                        nodesToAttach.add(node);
                        parents.add(m.parentID);
                    });
                    for (int i = 0; i < nodesToAttach.size(); i++) {
                        Node node = nodesToAttach.get(i);
                        int parentId = parents.get(i);
                        Node parentNode;
                        if (parentId == Node.ROOT_NODE_NETWORK_ID) {
                            parentNode = getParent().getRoot();
                        } else {
                            parentNode = nodeMap.get(parentId);
                        }
                        if (parentNode == null) {
                            throw new RuntimeException("parent node couldn't be found! Maybe the server didn't send it?");
                        }

                        parentNode.addChild(node);
                    }
                }

                // Reattachment
                if (nodeReattachList.size() > 0) {
                    List<Node> nodesToReattach = new ArrayList<>();
                    List<Integer> parents = new ArrayList<>();
                    nodeReattachList.forEach(m -> {
                        Node node = nodeMap.get(m.nodeID);
                        if (node == null) {
                            throw new RuntimeException("server asked a node to reattach, but it couldn't be found");
                        }
                        nodesToReattach.add(node);
                        parents.add(m.parentID);
                    });
                    for (int i = 0; i < nodesToReattach.size(); i++) {
                        Node node = nodesToReattach.get(i);
                        int parentId = parents.get(i);
                        Node parentNode;
                        if (parentId == Node.ROOT_NODE_NETWORK_ID) {
                            parentNode = getParent().getRoot();
                        } else {
                            parentNode = nodeMap.get(parentId);
                        }
                        if (parentNode == null) {
                            throw new RuntimeException("parent node for reattachment couldn't be found! Maybe the server didn't send it?");
                        }

                        // Adding the child will implicitly remove it from its original parent.
                        parentNode.addChild(node);
                    }
                }

                // Detachment
                if (nodeDetachList.size() > 0) {
                    List<Node> nodesToDetach = new ArrayList<>();
                    nodeDetachList.forEach(m -> {
                        Node node = nodeMap.get(m.nodeID);
                        if (node == null) {
                            Log.warn(getClass().getSimpleName(), "The server told us to detach a node but we didn't even know it ever existed.");
                        }
                        nodesToDetach.add(node);
                    });
                    nodesToDetach.forEach(n -> n.getParent().removeChild(n));
                }


                /* Component Synchronization */

                // Attachment
                if (componentAttachList.size() > 0) {
                    List<Component> componentsToAttach = new ArrayList<>();
                    List<Integer> parents = new ArrayList<>();
                    componentAttachList.forEach(m -> {
                        Component c = null;
                        try {
                            c = (Component) RepTable.getClassForClassID(m.repClassID).newInstance();
                        } catch (Exception ex) {
                            throw new RuntimeException("Couldn't instantiate a class that the server told us about", ex);
                        }

                        componentsToAttach.add(c);
                        c.setNetworkID(m.componentID);
                        parents.add(m.parentNodeID);
                        componentMap.put(m.componentID, c);
                    });
                    for (int i = 0; i < componentsToAttach.size(); i++) {
                        Component c = componentsToAttach.get(i);
                        int parentId = parents.get(i);

                        Node parentNode = nodeMap.get(parentId);
                        if (parentNode == null) {
                            throw new RuntimeException("Couldn't find the parent node for a new component.");
                        }
                        parentNode.addComponent(c);
                        if (c instanceof Transform) {
                            parentNode.setTransform((Transform) c);
                        }
                    }
                }

                // Reattachment
                if (componentReattachList.size() > 0) {
                    List<Component> componentsToReattach = new ArrayList<>();
                    List<Integer> parents = new ArrayList<>();
                    componentReattachList.forEach(m -> {
                        Component c = componentMap.get(m.componentID);
                        if (c == null) {
                            throw new RuntimeException("Couldn't find the component to reattach.");
                        }
                        componentsToReattach.add(c);
                        parents.add(m.parentNodeID);
                    });
                    for (int i = 0; i < componentsToReattach.size(); i++) {
                        Component c = componentsToReattach.get(i);
                        int parentID = parents.get(i);
                        Node parentNode;
                        if (parentID == -1) {
                            throw new RuntimeException("Server told us to attach a component to root ... ???");
                        }
                        parentNode = nodeMap.get(parentID);
                        if (parentNode == null) {
                            throw new RuntimeException("Server told us to attach a component to a node that doesn't exist locally.");
                        }
                        parentNode.addComponent(c);
                    }
                }

                // Detachment
                if (componentDetachList.size() > 0) {
                    componentDetachList.forEach(m -> {
                        Component c = componentMap.get(m.componentID);
                        if (c == null) {
                            Log.warn(getClass().getSimpleName(), "The server told us to detach a component we didn't know ever existed.");
                            return;
                        }
                        c.getParent().removeComponent(c);
                    });
                }

                /* Field Replication */
                if (fieldReplicateMessageList.size() > 0) {
                    fieldReplicateMessageList.forEach(m -> {
                        Component c = componentMap.get(m.componentID);
                        if (c == null) {
                            Log.warn(getClass().getSimpleName(), "The server gave us field replication info for a component that doesn't exist.");
                            return;
                        }
                        RepTable.getTableForType(c.getClass()).applyReplicationData(m.fieldReplicationData, c);
                    });
                }

                /* RPCs */
                if (rpcMessageList.size() > 0) {
                    rpcMessageList.forEach(m -> {
                        Component c = componentMap.get(m.targetNetworkID);
                        if (c == null) {
                            Log.warn(getClass().getSimpleName(), "The server wants to invoke an RPC on a component that doesn't exist.");
                            return;
                        }
                        try {
                            Method method = RepTable.getTableForType(c.getClass()).getRPCMethod(m.invocation.methodId);
                            method.invoke(c, m.invocation.arguments.toArray());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }

            // Send RPC messages
            if (!rpcsToSend.isEmpty()) {
                rpcsToSend.forEach(client::sendTCP);
                rpcsToSend.clear();
            }
        }
    }

    @Override
    public boolean doesProcessNodes() {
        return false;
    }
}
