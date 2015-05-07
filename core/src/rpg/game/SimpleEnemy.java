package rpg.game;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;

import java.util.Random;

/**
 * Created by Corin Hill on 5/6/15.
 */
public class SimpleEnemy extends Enemy {
    private StateMachine<SimpleEnemy> fsm;

    // Unused for now?
    private static float MOVE_SPEED = 4;

    private static float MAX_HEALTH = 100;

    // Inner radius of rectangle of vision
    private static float VISION_RADIUS = 5;

    // Desired distance from target
    private static float TARGET_LONG = 4;
    private static float TARGET_SHRT = 3;

    // Attention span in seconds, used for wandering
    private static float ATTENTION = 3;
    private float focus = 0;

    private Vector3 destination = null;

    private Node targetNode = null;
    private Vector3 targetDelta = null;
    private boolean firing = false;

    private static Random rng = new Random(System.currentTimeMillis());

    public SimpleEnemy() {
        health = MAX_HEALTH;
        fsm = new DefaultStateMachine<SimpleEnemy>(this,SimpleEnemyState.WANDER);
    }

    public void update(float deltaTime, Node target, Vector3 delta) {
        age(deltaTime);
        targetNode = target;
        targetDelta = delta;
        fsm.update();
    }


    public StateMachine<SimpleEnemy> getFSM() {return fsm;}

    public float getMoveSpeed() {return MOVE_SPEED;}

    public static float getVisionRadius() {return VISION_RADIUS;}

    public boolean isFocused() {return focus < ATTENTION;}
    public void refocus() {focus = 0;}
    public void age(float time) {focus += time;}

    public Vector3 getDestination() {return destination;}
    public void setDestination(Vector3 destination) {this.destination = destination;}

    public Node getTargetNode() {return targetNode;}
    public Vector3 getTargetDelta() {return targetDelta;}
    public boolean isTargetClose() {
        return targetDelta != null && targetDelta.len() < TARGET_SHRT;
    }
    public boolean isTargetFar() {
        return targetDelta != null && targetDelta.len() > TARGET_LONG;
    }

    public boolean isFiring() {return firing;}
    public void fire() {firing = true;}
    public void ceasefire() {firing = false;}

    public Random getRandom() {return rng;}
}
