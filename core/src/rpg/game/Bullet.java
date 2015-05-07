package rpg.game;

import com.badlogic.gdx.math.Vector3;
import rpg.scene.replication.Replicated;

/**
 * Created by Corin Hill on 5/6/15.
 */
public abstract class Bullet {
    @Replicated
    protected float age = 0;
    protected static float LIFETIME;

    @Replicated
    protected Vector3 moveDirection = new Vector3();

    public Vector3 getMoveDirection() {return moveDirection;}
    public abstract void setMoveDirection(Vector3 v);

    public abstract boolean isAlive();
    public float getLIFETIME() {return LIFETIME;}
    public void age(float time) {age += time;}

    public abstract float getDamage();
}
