package rpg.scene.components;

import rpg.scene.Node;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPCInvocation;
import rpg.scene.replication.RPCMessage;
import rpg.scene.replication.RepTable;
import rpg.scene.systems.NetworkingSceneSystem;

import java.lang.reflect.Method;
import java.util.Objects;

public abstract class Component {
    private Node parent;
    private int networkID = 0;

    private static int networkIDCounter = 0;

    public Component() {
        networkID = networkIDCounter++;
    }

    /**
     * Executes an RPC method in the appropriate context.
     * <p>
     * If the current context is equal to the RPC's context, or if there is no NetworkingSceneSystem in the scene,
     * then this method simply invokes that method via reflection.
     *
     * @param rpcName   the name of the method to invoke
     * @param arguments the arguments to pass to the method.
     */
    public void sendRPC(String rpcName, Object... arguments) {
        Objects.requireNonNull(rpcName);
        Objects.requireNonNull(arguments);
        NetworkingSceneSystem net = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        RepTable repTable = RepTable.getTableForType(getClass());
        Context context = repTable.getRPCContext(rpcName);
        Method m = repTable.getRPCMethod(rpcName);

        if (net == null || context.equals(net.getContext())) {
            // invoke the method since the context matches
            try {
                m = repTable.getRPCMethod(rpcName);
                Class<?> c = m.getDeclaringClass();
                m.invoke(c.cast(this), arguments);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // otherwise, tell the networking system to queue an RPCMessage
        RPCInvocation rpc = new RPCInvocation();
        rpc.methodId = repTable.getRPCMethodID(m);
        net.addRPCMessage(new RPCMessage(networkID, rpc));
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node n) {
        parent = n;
    }

    public int getNetworkID() {
        return networkID;
    }
}
