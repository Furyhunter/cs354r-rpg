package rpg.game;

import com.badlogic.gdx.math.Vector3;
import rpg.scene.replication.Replicated;

/**
 * Created by Corin Hill on 5/6/15.
 */
public abstract class Bullet {

    protected static float MOVE_SPEED;
    protected static float LIFETIME;

    @Replicated
    protected float age = 0;
    @Replicated
    protected Vector3 moveDirection = new Vector3();

    public Vector3 getMoveDirection() {return moveDirection;}
    public void setMoveDirection(Vector3 v) {moveDirection = v;}
    public boolean isAlive() {return age < LIFETIME;}
    public void age(float time) {age += time;}
}
