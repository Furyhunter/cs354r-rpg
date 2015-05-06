package rpg.game;

import com.badlogic.gdx.math.Vector3;
import rpg.scene.replication.Replicated;

/**
 * Created by Corin Hill on 5/6/15.
 */
public abstract class Bullet {
    private static float LIFETIME;
    private float age = 0;

    private static float MOVE_SPEED;

    @Replicated
    Vector3 moveDirection = new Vector3();

    public Vector3 getMoveDirection() {return moveDirection;}
    public void setMoveDirection(Vector3 v) {
        moveDirection.set(v.cpy().nor().scl(MOVE_SPEED));
    }

    public boolean isAlive() {return age < LIFETIME;}
    public void age(float time) {age += time;}

    public abstract float getDamage();
}
