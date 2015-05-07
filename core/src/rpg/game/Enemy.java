package rpg.game;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.replication.Replicated;

/**
 * Created by Corin Hill on 5/6/15.
 */
public abstract class Enemy {
    protected float health;

    public boolean isAlive() {return health > 0;}
    public void hurt(float damage) {health -= damage;}



}
