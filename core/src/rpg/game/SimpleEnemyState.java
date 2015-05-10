package rpg.game;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.components.SimpleEnemyComponent;

/**
 * Created by Corin Hill on 5/6/15.
 */
public enum SimpleEnemyState implements State<SimpleEnemyComponent> {
    /**
     * When no player is in the vision range,
     * wander in a direction until bored
     * Pick a new destination and repeat
     * Stay near home spawn point too
     */
    WANDER() {
        @Override
        public void update(SimpleEnemyComponent enemy) {
            Node target = enemy.getTargetNode();
            // Modify destination, if enemy is bored
            if (!enemy.isFocused() || enemy.getDestination() == null) {
                float xOff = MathUtils.random(-.5f,.5f);
                float yOff = MathUtils.random(-.5f,.5f);
                if (enemy.isHomeFar()) {
                    xOff = enemy.getHomePosition().x - enemy.getParent().getTransform().getWorldPosition().x;
                    yOff = enemy.getHomePosition().y - enemy.getParent().getTransform().getWorldPosition().y;
                }

                enemy.setDestination(new Vector3(xOff, yOff, 0));
                enemy.refocus();
            }

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
        public void update(SimpleEnemyComponent enemy) {
            Node target = enemy.getTargetNode();


            Vector3 v = enemy.getTargetDelta();
            if (v != null) {
                enemy.setDestination(v);
            }

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
        public void  update(SimpleEnemyComponent enemy) {
            Node target = enemy.getTargetNode();

            enemy.setFiring(true);

            if (target != null) {
                if (enemy.isTargetFar()) {
                    enemy.getFSM().changeState(CHASE);
                }
            } else {
                enemy.getFSM().changeState(WANDER);
            }
        }
        @Override
        public void exit(SimpleEnemyComponent enemy) {
            super.exit(enemy);
            enemy.setFiring(false);
        }

    };
    @Override
    public void enter(SimpleEnemyComponent enemy) {}
    @Override
    public void update(SimpleEnemyComponent enemy) {}
    @Override
    public void exit(SimpleEnemyComponent enemy) {
        enemy.setDestination(null);
        enemy.refocus();
    }
    @Override
    public boolean onMessage(SimpleEnemyComponent enemy, Telegram telegram) {
        return false;
    }
}
