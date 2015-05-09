package rpg.scene.components;

import rpg.scene.replication.RPC;

public interface Killable {
    @RPC(target = RPC.Target.Multicast)
    void kill();
}
