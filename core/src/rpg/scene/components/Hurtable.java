package rpg.scene.components;

import rpg.scene.replication.RPC;

public interface Hurtable {
    @RPC(target = RPC.Target.Server)
    void hurt(Component cause, float baseDamage);
}
