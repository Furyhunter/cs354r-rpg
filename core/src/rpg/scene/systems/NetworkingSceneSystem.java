package rpg.scene.systems;

import rpg.scene.replication.Context;
import rpg.scene.replication.RPCMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class NetworkingSceneSystem extends AbstractSceneSystem {

    protected List<RPCMessage> rpcMessages = new ArrayList<>();
    protected List<RPCMessage> multicastRPCMessages = new ArrayList<>();

    public abstract Context getContext();

    /**
     * Queue a message to send.
     *
     * @param m the RPCMessage.
     */
    public void addRPCMessage(RPCMessage m) {
        Objects.requireNonNull(m);
        rpcMessages.add(m);
    }

    /**
     * Queue a message to send to all destinations.
     *
     * @param m the RPCMessage to send.
     */
    public void addMulticastRPCMessage(RPCMessage m) {
        Objects.requireNonNull(m);
        multicastRPCMessages.add(m);
    }

    /**
     * Process a regular RPC message. The destination should be derived from the RPC itself.
     * For example, in a Client context, we should always send RPCs to the server, but only if
     * we actually own the Node we want to send an RPC on a component for.
     *
     * @param m the message to process.
     */
    public abstract void processRPC(RPCMessage m);

    /**
     * Process a multicast RPC message.
     *
     * @param m the message to process.
     */
    public abstract void processMulticastRPC(RPCMessage m);

    /**
     * Whether or not we should process RPC messages right now.
     *
     * @return true if should process RPCs.
     */
    public abstract boolean canProcessRPCs();

    @Override
    public void endProcessing() {
        if (canProcessRPCs()) {
            rpcMessages.forEach(this::processRPC);
            multicastRPCMessages.forEach(this::processMulticastRPC);

            rpcMessages.clear();
            multicastRPCMessages.clear();
        }
    }
}
