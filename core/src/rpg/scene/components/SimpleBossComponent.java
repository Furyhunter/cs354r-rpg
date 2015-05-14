package rpg.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.NodeFactory;
import rpg.scene.replication.Context;
import rpg.scene.systems.NetworkingSceneSystem;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Optional;

/**
 * Created by Corin Hill on 5/13/15.
 */
public class SimpleBossComponent extends Component implements Steppable, Killable {
    private static float VISION_RADIUS = 15;
    private static float DROP_RATE = 0.7f;

    private Node target = null;
    private Vector3 relPos = new Vector3();
    private int state = WAIT;

    private float timer = 0;
    private static final float WAIT_TIME = 1;

    private int counter = 0;
    private int PHASE_COUNT = 0;


    public static final int WAIT = 0;
    public static final int ATT1 = 1;
    public static final int ATT2 = 2;
    public static final int ATT3 = 3;

    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        if (nss.getContext() == Context.Server) {
            switch (state) {
                case ATT1:
                    PHASE_COUNT = 4;
                    timer += deltaTime;
                    if (timer > WAIT_TIME) {
                        updateRelPos();
                        Vector3 off = new Vector3();
                        if (relPos.x != 0) {
                            off.y = 0.5f;
                        }
                        if (relPos.y != 0) {
                            off.x = 0.5f;
                        }
                        if (relPos.x == relPos.y) {
                            off.x *= -1;
                        }
                        for (int i = -3; i < 4; ++i) {
                            generateSmallBullet(relPos, off.cpy().scl(i));
                        }
                        timer = 0;
                        ++counter;
                        if (counter > PHASE_COUNT) {
                            checkTarget();
                            counter = 0;
                            if (target != null) {
                                state = ATT2;
                            } else {
                                state = WAIT;
                            }
                        }
                    }
                    break;
                case ATT2:
                    PHASE_COUNT = 2;
                    timer += deltaTime;
                    if (timer > WAIT_TIME) {
                        updateRelPos();
                        Vector3 off = new Vector3();
                        if (relPos.x != 0) {
                            off.y = 1.5f;
                        }
                        if (relPos.y != 0) {
                            off.x = 1.5f;
                        }
                        if (relPos.x == relPos.y) {
                            off.x *= -1;
                        }
                        for (int i = -2; i < 3; ++i) {
                            generateBomb(relPos, off.cpy().scl(i));
                        }
                        timer = 0;
                        ++counter;
                        if (counter > PHASE_COUNT) {
                            checkTarget();
                            counter = 0;
                            if (target != null) {
                                state = ATT3;
                            } else {
                                state = WAIT;
                            }
                        }
                    }
                    break;
                case ATT3:
                    PHASE_COUNT = 26;
                    timer += deltaTime;
                    if (timer > (WAIT_TIME / 10)) {
                        updateRelPos();
                        Vector3 off = new Vector3();
                        if (relPos.x != 0) {
                            off.y = 0.5f;
                        }
                        if (relPos.y != 0) {
                            off.x = 0.5f;
                        }
                        if (relPos.x == relPos.y) {
                            off.x *= -1;
                        }
                        if (counter < 9) {
                            generateSmallBullet(relPos, off.scl(counter - 4));
                        } else if (counter < 18) {
                            generateSmallBullet(relPos, off.scl(13 - counter));
                        } else {
                            generateSmallBullet(relPos, off.scl(counter - 22));
                        }
                        timer = 0;
                        ++counter;
                        if (counter > PHASE_COUNT) {
                            checkTarget();
                            counter = 0;
                            if (target != null) {
                                state = ATT1;
                            } else {
                                state = WAIT;
                            }
                        }
                    }
                    break;
                case WAIT:
                    timer += deltaTime;
                    if (timer > WAIT_TIME) {
                        checkTarget();
                        timer = 0;
                        if (target != null) {
                            state = ATT1;
                        }
                    }
                    break;
                default:
                    target = null;
                    state = WAIT;
                    timer = 0;
                    break;
            }
        }
    }

    private void generateSmallBullet(Vector3 moveDirection, Vector3 offset) {
        Node bulletNode = new Node();
        getParent().getScene().getRoot().addChild(bulletNode);

        SimpleBulletComponent bulletComponent = new SimpleBulletComponent();
        bulletComponent.setMoveDirection(moveDirection);
        bulletComponent.setCreator(getParent());

        RectangleRenderer r = new RectangleRenderer();
        r.setColor(Color.PINK);
        r.setSize(new Vector2(0.1f, 0.1f));

        bulletNode.addComponent(bulletComponent);
        bulletNode.addComponent(r);

        Transform tBullet = bulletNode.getTransform();
        Transform tSelf = getParent().getTransform();
        tBullet.setPosition(tSelf.getWorldPosition().cpy().add(offset));
        tBullet.setRotation(tSelf.getWorldRotation().cpy());
        tBullet.translate(0, 0, 0.5f);
    }

    private void generateBomb(Vector3 moveDirection, Vector3 offset) {
        Node bulletNode = new Node();
        getParent().getScene().getRoot().addChild(bulletNode);

        MissileComponent missileComponent = new MissileComponent();
        missileComponent.setMoveDirection(moveDirection);
        missileComponent.setCreator(getParent());

        SpriteRenderer spriteRenderer = new SpriteRenderer();
        spriteRenderer.setTexture("sprites/orange.png");
        spriteRenderer.setDimensions(new Vector2(0.4f, 0.4f));
        spriteRenderer.setBillboard(false);

        bulletNode.addComponent(missileComponent);
        bulletNode.addComponent(spriteRenderer);

        Transform tBullet = bulletNode.getTransform();
        Transform tSelf = getParent().getTransform();
        tBullet.setPosition(tSelf.getWorldPosition().cpy().add(offset));
        tBullet.setRotation(tSelf.getWorldRotation().cpy());
        tBullet.translate(0, 0, 0.5f);
    }

    private void updateRelPos() {
        if (target != null) {
            Vector3 wpTaraget = target.getTransform().getWorldPosition();
            Vector3 wpSelf = getParent().getTransform().getWorldPosition();
            Rectangle rSelf = getParent().findComponent(Spatial2D.class).getRectangle();

            if (wpSelf.x - rSelf.getWidth() > wpTaraget.x) relPos.x = -1;
            else if (wpSelf.x + rSelf.getWidth() < wpTaraget.x) relPos.x = 1;
            else relPos.x = 0;

            if (wpSelf.y - rSelf.getHeight() > wpTaraget.y) relPos.y = -1;
            else if (wpSelf.y + rSelf.getWidth() < wpTaraget.y) relPos.y = 1;
            else relPos.y = 0;
        }
    }

    private void checkTarget() {
        if (target == null || target.getParent() == null || target.findComponent(UnitComponent.class) == null
                || target.findComponent(UnitComponent.class).getHealth() <= 0) {
            Node2DQuerySystem nqs = getParent().getScene().findSystem(Node2DQuerySystem.class);
            Vector3 wp = getParent().getTransform().getWorldPosition();
            Rectangle r = new Rectangle(wp.x-VISION_RADIUS,wp.y-VISION_RADIUS,
                    VISION_RADIUS*2,VISION_RADIUS*2);
            if (nqs != null) {
                Optional<SimplePlayerComponent> p = nqs.queryNodesInArea(r).stream()
                        .map(n -> n.findComponent(SimplePlayerComponent.class))
                        .filter(n -> n != null).findAny();
                if (p.isPresent()) target = p.get().getParent();
                else target = null;
            }
        }
    }

    private void generateEXP() {
        Transform t = getParent().getTransform();
        for (int i = 0; i < MathUtils.random(5,10); ++i) {
            Node n = NodeFactory.createEXPDrop(getParent().findRoot(), false);
            n.getTransform().setPosition(new Vector3(t.getWorldPosition().x,t.getWorldPosition().y,0.1f));

            PickupComponent p = new PickupComponent();
            p.setItem(PickupComponent.EXP);
            ArcToGroundComponent a = new ArcToGroundComponent();

            n.addComponent(p);
            n.addComponent(a);
        }

    }

    private void generateDrop() {
        Transform t = getParent().getTransform();
        for (int i = 0; i < MathUtils.random(0,4); ++i) {
            int item;
            if (MathUtils.random() < 0.25) {
                item = PickupComponent.HEAL;
            } else {
                item = PickupComponent.BOMB;
            }
            Node n = NodeFactory.createDrop(getParent().findRoot(), false, item);
            n.getTransform().setPosition(new Vector3(t.getWorldPosition().x,t.getWorldPosition().y,0.005f));

            PickupComponent p = new PickupComponent();
            p.setItem(item);
            ArcToGroundComponent a = new ArcToGroundComponent();

            n.addComponent(p);
            n.addComponent(a);
        }
    }

    public void kill() {
        generateEXP();
        if (MathUtils.random() < DROP_RATE) {
            generateDrop();
        }
        getParent().removeFromParent();
    }
}
