package rpg.game;

/**
 * Created by Corin Hill on 5/6/15.
 */
public class SimpleBullet extends Bullet {
    public static float LIFETIME = 2;

    public static float MOVE_SPEED = 6;

    @Override
    public float getDamage() {
       return 10;
    }
}
