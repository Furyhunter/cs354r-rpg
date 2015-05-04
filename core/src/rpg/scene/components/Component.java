package rpg.scene.components;

import rpg.scene.Node;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPC;
import rpg.scene.replication.RPCMessage;
import rpg.scene.replication.RepTable;
import rpg.scene.systems.NetworkingSceneSystem;

import java.util.Objects;

public abstract class Component {
    private Node parent;
    private int networkID = 0;

    private static int networkIDCounter = 0;
    private static int localNetworkIDCounter = Integer.MIN_VALUE;

    public Component() {
        networkID = networkIDCounter++;
    }

    public static <T extends Component> T createLocalComponent(Class<T> type) {
        try {
            T ret = type.newInstance();
            ret.setNetworkID(localNetworkIDCounter++);
            networkIDCounter--;
            return ret;
        } catch (Exception e) {
            throw new RuntimeException("Exception when instantiating via createLocalComponent: " + e.getMessage(), e);
        }
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
        Class<?> actualClassOfThis = getClass();

        if (net == null) {
            // always invoke
            repTable.invokeMethod(actualClassOfThis.cast(this), rpcName, arguments);
            return;
        }

        // check context
        Context context = net.getContext();
        if (context == Context.Server) {
            if (target == RPC.Target.Multicast) {
                repTable.invokeMethod(actualClassOfThis.cast(this), rpcName, arguments);
                net.addMulticastRPCMessage(new RPCMessage(networkID, repTable.getRPCInvocation(rpcName, arguments)));
            } else if (target == RPC.Target.Client) {
                net.addRPCMessage(new RPCMessage(networkID, repTable.getRPCInvocation(rpcName, arguments)));
            } else {
                repTable.invokeMethod(actualClassOfThis.cast(this), rpcName, arguments);
            }
        } else if (context == Context.Client) {
            if (target == RPC.Target.Client) {
                repTable.invokeMethod(actualClassOfThis.cast(this), rpcName, arguments);
            } else if (target == RPC.Target.Server) {
                net.addRPCMessage(new RPCMessage(networkID, repTable.getRPCInvocation(rpcName, arguments)));
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

    public void setNetworkID(int networkID) {
        this.networkID = networkID;
    }

    /**
     * Called before replication fields are applied. (On the client, basically.)
     */
    public void onPreApplyReplicateFields() {

    }

    /**
     * Called after replication fields are applied.
     */
    public void onPostApplyReplicatedFields() {

    }

    /**
     * Whether or not this component will always have a {@link rpg.scene.kryo.FieldReplicateMessage} sent, even if the
     * fields haven't changed at all. This will force {@link #onPreApplyReplicateFields()} and family to be called
     * every tick. An empty FieldReplicateMessage will add a minimum of 6 bytes to the tick, based on the number of
     * replicated fields.
     *
     * @return whether or not to always sent field replication information.
     */
    public boolean isAlwaysFieldReplicated() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Component component = (Component) o;

        return networkID == component.networkID;

    }

    @Override
    public int hashCode() {
        return networkID;
    }
}
