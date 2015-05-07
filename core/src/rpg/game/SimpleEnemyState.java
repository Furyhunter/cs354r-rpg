package rpg.game;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;

import java.util.Random;

/**
 * Created by Corin Hill on 5/6/15.
 */
public enum SimpleEnemyState implements State<SimpleEnemy> {
    /**
     * When no player is in the vision range,
     * wander in a direction until bored
     * Pick a new destination and repeat
     */
    WANDER() {
        @Override
        public void update(SimpleEnemy enemy) {
            Node target = enemy.getTargetNode();
            // Modify destination, if enemy is bored
            if (!enemy.isFocused()) {
                Random rng = new Random();
                // Hard coding scale of deflection
                // Or lack thereof, as it is 1
                float xOff = (rng.nextFloat() - 1);
                float yOff = (rng.nextFloat() - 1);

                if (enemy.getDestination() != null) {
                    // Alter path slightly
                    enemy.getDestination().add(xOff,yOff,0);
                } else {
                    // Set a new random destination
                    // And hard code more scalars
                    enemy.getDestination().set(xOff*15,yOff*15,0);
                }
                enemy.refocus();
            }

            super.update(enemy);
            if (target != null) {
                if (enemy.isTargetFar()) {
                    enemy.getFSM().changeState(CHASE);
                } else {
                    enemy.getFSM().changeState(ATTACK);
                }
            }
        }
    },
    /**
     * When a player is Visible but not close enough to shoot
     * Move towards the player
     */
    CHASE() {
        @Override
        public void update(SimpleEnemy enemy) {
            Node target = enemy.getTargetNode();

            enemy.setDestination(enemy.getTargetDelta());

            super.update(enemy);
            if (target != null) {
                if (!enemy.isTargetFar()) {
                    enemy.getFSM().changeState(ATTACK);
                }
            } else {
                enemy.getFSM().changeState(WANDER);
            }
        }
    },
    ATTACK() {
        @Override
        public void  update(SimpleEnemy enemy) {
            Node target = enemy.getTargetNode();

            enemy.fire();

            super.update(enemy);
            if (target != null) {
                if (enemy.isTargetFar()) {
                    enemy.getFSM().changeState(CHASE);
                }
            } else {
                enemy.getFSM().changeState(WANDER);
            }
        }
        @Override
        public void exit(SimpleEnemy enemy) {
            super.exit(enemy);
            enemy.ceasefire();
        }

    },
    DEAD() {
    };
    @Override
    public void enter(SimpleEnemy enemy) {}
    @Override
    public void update(SimpleEnemy enemy) {
        if (!enemy.isAlive()) {
            enemy.getFSM().changeState(DEAD);
        }
    }
    @Override
    public void exit(SimpleEnemy enemy) {
        enemy.setDestination(null);
    }
    @Override
    public boolean onMessage(SimpleEnemy enemy, Telegram telegram) {
        return false;
    }
}
