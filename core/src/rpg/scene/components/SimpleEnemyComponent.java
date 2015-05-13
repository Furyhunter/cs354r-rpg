package rpg.scene.components;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.game.SimpleEnemyState;
import rpg.scene.Node;
import rpg.scene.NodeFactory;
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

    private static float DROP_RATE = 01f;

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

    private boolean shadowCreated = false;

    public SimpleEnemyComponent() {
        fsm = new DefaultStateMachine(this, SimpleEnemyState.WANDER);
    }

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Objects.requireNonNull(nss);

        Transform t = getParent().getTransform();
        if (nss.getContext() == Context.Server) {
            targetNode = findTargetNode();
            focus += deltaTime;
            fsm.update();

            if (destination != null) {
                Vector3 v = t.getPosition().cpy().add(destination.cpy().nor().scl(MOVE_SPEED).scl(deltaTime));
                v.z = t.getPosition().z;
                t.setPosition(v);
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

            if (!shadowCreated) {
                Node n = NodeFactory.makeShadowNode(getParent(), true);
                n.getTransform().translate(0, 0, 0.005f);
                shadowCreated = true;
            }

        }
    }

    private void generateEXP() {
        Transform t = getParent().getTransform();
        for (int i = 0; i < MathUtils.random(1,4); ++i) {
            Node n = NodeFactory.createEXPDrop(getParent().findRoot(),false);
            n.getTransform().setPosition(new Vector3(t.getWorldPosition().x,t.getWorldPosition().y,0.1f));

            PickupComponent p = new PickupComponent();
            p.setItem(PickupComponent.EXP);
            ArcToGroundComponent a = new ArcToGroundComponent();

            n.addComponent(p);
            n.addComponent(a);
        }
    }

    private void generateDrop() {
        Node dropNode = new Node();
        getParent().getScene().getRoot().addChild(dropNode);

        SpriteRenderer sr = new SpriteRenderer();
        sr.setTexture("sprites/orange.png");
        sr.setDimensions(new Vector2(0.3f, 0.3f));
        dropNode.addComponent(sr);

        PickupComponent p = new PickupComponent();
        p.setItem(PickupComponent.BOMB);
        dropNode.addComponent(p);

        ArcToGroundComponent a = new ArcToGroundComponent();
        dropNode.addComponent(a);

        Transform tDrop = dropNode.getTransform();
        Transform tSelf = getParent().getTransform();

        tDrop.setPosition(tSelf.getWorldPosition());
        tDrop.setRotation(tSelf.getWorldRotation());
        tDrop.translate(0, 0, 0.005f);
    }

    private void generateBullet(Vector3 v) {
        Node bulletNode = new Node();
        getParent().getScene().getRoot().addChild(bulletNode);

        SimpleBulletComponent bulletComponent = new SimpleBulletComponent();
        bulletComponent.setMoveDirection(v);
        bulletComponent.setCreator(getParent());
        bulletNode.addComponent(bulletComponent);

        RectangleRenderer r = new RectangleRenderer();
        r.setColor(Color.PINK);
        r.setSize(new Vector2(0.1f, 0.1f));
        bulletNode.addComponent(r);

        Transform tBullet = bulletNode.getTransform();
        Transform tSelf = getParent().getTransform();

        tBullet.setPosition(tSelf.getWorldPosition());
        tBullet.setRotation(tSelf.getWorldRotation());
        tBullet.translate(0, 0, 0.5f);
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
        generateEXP();
        if (MathUtils.random() < DROP_RATE) {
            generateDrop();
        }
        getParent().removeFromParent();
    }
}
