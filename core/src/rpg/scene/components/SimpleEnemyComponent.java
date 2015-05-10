package rpg.scene.components;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.game.SimpleEnemyState;
import rpg.scene.Node;
import rpg.scene.replication.Context;
import rpg.scene.systems.NetworkingSceneSystem;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by Corin Hill on 5/6/15.
 */
public class SimpleEnemyComponent extends Component implements Steppable, Killable {
    private StateMachine<SimpleEnemyComponent> fsm;

    private float shootTimer = 0;
    private static float SHOOT_UPDATE_THRESHOLD = 1f / 3;

    private float moveTimer = 0;
    private static float MOVE_SPEED = 4;
    // Inner radius of rectangle of vision
    private static float VISION_RADIUS = 10;
    // Desired distance from target
    private static float ATTACK_RADIUS = 5;
    // Allowed distance from home spawn point
    private static float WANDER_RADIUS = 16;

    // Attention span in seconds, used for wandering
    private static float ATTENTION = 3;
    private float focus = 0;

    private Vector3 home = new Vector3();

    private Node targetNode = null;
    private Vector3 destination = null;
    private boolean firing = false;

    private Vector3 oldPosition = null;
    private Vector3 newPosition = null;

    private boolean lerpTargetChanged = false;

    public SimpleEnemyComponent() {
        fsm = new DefaultStateMachine(this, SimpleEnemyState.WANDER);
    }

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Objects.requireNonNull(nss);

        Transform t = getParent().getTransform();
        Vector3 p = t.getPosition().cpy();
        if (nss.getContext() == Context.Server) {
            targetNode = findTargetNode();
            focus += deltaTime;
            fsm.update();

            if (destination != null) {
                t.setPosition(p.add(destination.cpy().nor().scl(MOVE_SPEED).scl(deltaTime)));
            }
            if (firing) {
                if (shootTimer >= SHOOT_UPDATE_THRESHOLD || shootTimer == 0) {
                    generateBullet(getTargetDelta());
                    shootTimer = 0;
                }
                shootTimer += deltaTime;
            } else {
                if (shootTimer >= SHOOT_UPDATE_THRESHOLD) {
                    shootTimer = 0;
                }
                if (shootTimer != 0) {
                    shootTimer += deltaTime;
                }
            }

        } else if (nss.getContext() == Context.Client) {
            if (oldPosition != null) {
                moveTimer += deltaTime;
                if (lerpTargetChanged) {
                    moveTimer = 0;
                    lerpTargetChanged = false;
                }
                t.setPosition(oldPosition.cpy().lerp(newPosition, moveTimer / nss.getTickDeltaTime()));
            } else if (newPosition != null) {
                t.setPosition(newPosition);
            }

        }
    }

    private void generateBullet(Vector3 v) {
        Node bulletNode = new Node();
        getParent().getScene().getRoot().addChild(bulletNode);

        SimpleBulletComponent bulletComponent = new SimpleBulletComponent();
        bulletComponent.setMoveDirection(v);
        bulletComponent.setCreator(getParent());
        RectangleRenderer r = new RectangleRenderer();
        r.setColor(Color.PINK);
        r.setSize(new Vector2(0.1f, 0.1f));
        r.setTransparent(true);
        bulletNode.addComponent(bulletComponent);
        bulletNode.addComponent(r);

        Transform tBullet = bulletNode.getTransform();
        Transform tSelf = getParent().getTransform();

        tBullet.setPosition(tSelf.getWorldPosition());
        tBullet.setRotation(tSelf.getWorldRotation());
    }

    private Node findTargetNode() {
        Node2DQuerySystem n2qs = getParent().getScene().findSystem(Node2DQuerySystem.class);
        Objects.requireNonNull(n2qs);

        Vector3 p = getParent().getTransform().getWorldPosition();
        Rectangle r = new Rectangle(p.x - VISION_RADIUS,p.y - VISION_RADIUS,
                                    VISION_RADIUS * 2f,VISION_RADIUS * 2f);

        Optional<Node> closest =
                n2qs.queryNodesInArea(r).parallelStream().filter(n ->
                                n.findComponent(SimplePlayerComponent.class) != null
                ).min((n1, n2) ->
                                Comparator.<Float>naturalOrder().compare(
                                        p.dst(n1.getTransform().getWorldPosition()),
                                        p.dst(n2.getTransform().getWorldPosition())
                                )
                );
        if (closest.isPresent())
            return closest.get();
        else
            return null;
    }

    public StateMachine<SimpleEnemyComponent> getFSM() {return fsm;}
    public boolean isFocused() {return focus < ATTENTION;}
    public void refocus() {focus = 0;}
    public Vector3 getHomePosition() {return home;}
    public boolean isHomeFar() {
        if (home == null) return false;
        return home.dst(getParent().getTransform().getWorldPosition()) > WANDER_RADIUS;
    }
    public void setHomePosition(Vector3 home) {this.home.set(home);}
    public Node getTargetNode() {return targetNode;}
    public boolean isTargetFar() {
        if (targetNode == null) return false;
        return targetNode.getTransform().getWorldPosition().dst(getParent().getTransform().getWorldPosition()) > ATTACK_RADIUS;
    }
    public Vector3 getTargetDelta() {
        if (targetNode == null) return null;
        return targetNode.getTransform().getWorldPosition().cpy().sub(getParent().getTransform().getWorldPosition());
    }
    public Vector3 getDestination() {return destination;}
    public void setDestination(Vector3 v) {this.destination = v;}
    public void setFiring(boolean fire) {this.firing = fire;}

    @Override
    public void onPreApplyReplicateFields() {
        Transform t = getParent().getTransform();
        oldPosition = newPosition == null ? null : newPosition.cpy();
        // The transform has already been updated by now.
        newPosition = t.getPosition().cpy();
        lerpTargetChanged = true;
    }

    @Override
    public boolean isAlwaysFieldReplicated() {
        return true;
    }

    @Override
    public void kill() {
        getParent().removeFromParent();
    }
}
