package rpg.game;

/**
 * Created by Corin Hill on 5/6/15.
 */
public abstract class Enemy {
    protected float health;

    public boolean isAlive() {return health > 0;}
    public void hurt(float damage) {health -= damage;}
}
