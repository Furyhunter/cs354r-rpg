package rpg.scene.components;

import rpg.scene.Node;
import rpg.scene.replication.*;
import rpg.scene.systems.NetworkingSceneSystem;

import java.lang.reflect.Method;
import java.util.Arrays;
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
        RPC.Target target = repTable.getRPCTarget(rpcName);
        Method m = repTable.getRPCMethod(rpcName);
        Class<?> actualClassOfThis = m.getDeclaringClass();

        if (net == null) {
            // always invoke
            try {
                m.invoke(actualClassOfThis.cast(this), arguments);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // check context
        Context context = net.getContext();
        if (context == Context.Server) {
            if (target == RPC.Target.Multicast) {
                // invoke
                try {
                    m.invoke(actualClassOfThis.cast(this), arguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RPCInvocation rpc = new RPCInvocation();
                rpc.methodId = repTable.getRPCMethodID(m);
                rpc.arguments = Arrays.asList(arguments);
                net.addMulticastRPCMessage(new RPCMessage(networkID, rpc));
            } else if (target == RPC.Target.Client) {
                RPCInvocation rpc = new RPCInvocation();
                rpc.methodId = repTable.getRPCMethodID(m);
                rpc.arguments = Arrays.asList(arguments);
                net.addRPCMessage(new RPCMessage(networkID, rpc));
            } else {
                // invoke
                try {
                    m.invoke(actualClassOfThis.cast(this), arguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (context == Context.Client) {
            if (target == RPC.Target.Client) {
                // invoke
                try {
                    m.invoke(actualClassOfThis.cast(this), arguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (target == RPC.Target.Server) {
                RPCInvocation rpc = new RPCInvocation();
                rpc.methodId = repTable.getRPCMethodID(m);
                rpc.arguments = Arrays.asList(arguments);
                net.addRPCMessage(new RPCMessage(networkID, rpc));
            }
        }
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
