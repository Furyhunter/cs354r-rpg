package rpg.scene.systems;

import rpg.scene.replication.Context;
import rpg.scene.replication.RPCMessage;

public abstract class NetworkingSceneSystem extends AbstractSceneSystem {

    public abstract Context getContext();

    /**
     * Queue a message to send.
     *
     * @param m the RPCMessage.
     */
    public abstract void addRPCMessage(RPCMessage m);
    /**
     * Queue a message to send to all destinations.
     *
     * @param m the RPCMessage to send.
     */
    public abstract void addMulticastRPCMessage(RPCMessage m);
}
