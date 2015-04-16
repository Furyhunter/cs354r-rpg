package rpg.scene.systems;

import rpg.scene.replication.Context;
import rpg.scene.replication.RPCMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class NetworkingSceneSystem extends AbstractSceneSystem {
    protected List<RPCMessage> rpcMessages = new ArrayList<>();

    public abstract Context getContext();

    public void addRPCMessage(RPCMessage m) {
        Objects.requireNonNull(m);
        rpcMessages.add(m);
    }
}
