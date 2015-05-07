package rpg.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
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
public class SimpleEnemyComponent extends Component implements Steppable {
    private SimpleEnemy enemy;

    private Vector3 destination;
    private Vector3 target;

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
            Node n = getTargetNode();
            target = (n==null)? null: n.getTransform().getWorldPosition().cpy().sub(wp);
            enemy.update(deltaTime,n,target);
            destination = enemy.getDestination();

            if (destination != null) {
                // should take into account move speed...
                t.setPosition(p.lerp(destination, deltaTime));
            }

            if (enemy.isFiring() && target != null) {
                generateBullet(target);
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

        SimpleBulletComponent s = new SimpleBulletComponent();
        s.setMoveDirection(v);
        RectangleRenderer r = new RectangleRenderer();
        r.setColor(Color.PINK);
        bulletNode.addComponent(s);
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
}
