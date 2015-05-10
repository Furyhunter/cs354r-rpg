package rpg.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.game.SimpleEnemy;
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
    private SimpleEnemy enemy = new SimpleEnemy();

    private float shootTimer = 0;
    private static float SHOOT_UPDATE_THRESHOLD = 1f / 3;

    private float moveTimer = 0;

    private Vector3 oldPosition = null;
    private Vector3 newPosition = null;

    private boolean lerpTargetChanged = false;

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Objects.requireNonNull(nss);

        Transform t = getParent().getTransform();
        Vector3 p = t.getPosition().cpy();
        Vector3 wp = t.getWorldPosition().cpy();
        if (nss.getContext() == Context.Server) {
            Node targetNode = getTargetNode();
            Vector3 targetDelta = (targetNode==null)? null: targetNode.getTransform().getWorldPosition().cpy().sub(wp);
            enemy.update(deltaTime,targetNode,targetDelta);

            if (enemy.getDestination() != null) {
                t.setPosition(p.lerp(enemy.getDestination(), deltaTime));
            }
            if (enemy.isFiring() && targetDelta != null) {
                if (shootTimer >= SHOOT_UPDATE_THRESHOLD || shootTimer == 0) {
                    generateBullet(targetDelta);
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

    private Node getTargetNode() {
        Node2DQuerySystem n2qs = getParent().getScene().findSystem(Node2DQuerySystem.class);
        Objects.requireNonNull(n2qs);

        Vector3 p = getParent().getTransform().getWorldPosition();
        Rectangle r = new Rectangle(p.x,p.y,enemy.getVisionRadius(),enemy.getVisionRadius());

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
