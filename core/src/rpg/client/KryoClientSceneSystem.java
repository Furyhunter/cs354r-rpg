package rpg.client;

import rpg.scene.Node;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPCMessage;
import rpg.scene.systems.NetworkingSceneSystem;

public class KryoClientSceneSystem extends NetworkingSceneSystem {
    @Override
    public Context getContext() {
        return Context.Client;
    }

    @Override
    public void processRPC(RPCMessage m) {

    }

    @Override
    public void processMulticastRPC(RPCMessage m) {

    }

    @Override
    public boolean canProcessRPCs() {
        return false;
    }

    @Override
    public void processNode(Node n, float deltaTime) {

    }
}
