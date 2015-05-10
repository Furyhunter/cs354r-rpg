package rpg.game;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by Corin Hill on 5/6/15.
 */
public class SimpleBullet extends Bullet {
    public SimpleBullet() {
        LIFETIME = 2;
        MOVE_SPEED = 8;
    }

    @Override
    public void setMoveDirection(Vector3 v) {
        moveDirection.set(v.cpy().nor().scl(MOVE_SPEED));
    }
}
