package rpg.scene.components;

import com.badlogic.gdx.math.Vector3;
import rpg.game.Bullet;
import rpg.game.SimpleBullet;
import rpg.scene.Node;
import rpg.scene.replication.Context;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.NetworkingSceneSystem;

import java.util.Objects;

/**
 * Created by Corin Hill on 5/6/15.
 */
public class SimpleBulletComponent extends Component implements Steppable{

    @Replicated
    Bullet bullet = new SimpleBullet();

    private Vector3 oldPosition = null;
    private Vector3 newPosition = null;

    private float moveTimer = 0;
    private boolean lerpTargetChanged = false;

    public SimpleBulletComponent() {}

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Objects.requireNonNull(nss);

        Transform t = getParent().getTransform();
        Vector3 p = t.getPosition().cpy();

        //System.out.println(bullet.getMoveDirection());
        if (nss.getContext() == Context.Server) {
            if (bullet.isAlive()) {
                t.setPosition(p.add(bullet.getMoveDirection().cpy().scl(deltaTime)));
                bullet.age(deltaTime);
            } else {
                // Leaving destruction to the GC
                Node n = getParent();
                n.getParent().removeChild(n);
            }
        } else if (nss.getContext() == Context.Client) {
            if (bullet.isAlive()) {
                moveTimer += deltaTime;
                if (lerpTargetChanged) {
                    if (oldPosition != null) {
                        newPosition = p.cpy().add(p.cpy().sub(oldPosition));
                    }
                    oldPosition = p.cpy();
                    moveTimer = 0;
                    lerpTargetChanged = false;
                }

                if (oldPosition != null && newPosition != null) {
                    // Extrapolate server side motion
                    t.setPosition(oldPosition.cpy().lerp(newPosition, moveTimer / nss.getTickDeltaTime()));
                } else {
                    t.setPosition(p.cpy().add(bullet.getMoveDirection().cpy().scl(deltaTime)));
                }
                bullet.age(deltaTime);
            }
        }
    }

    @Override
    public void onPostApplyReplicatedFields() {
        lerpTargetChanged = true;
    }
    @Override
    public boolean isAlwaysFieldReplicated() {return true;}

    public Vector3 getMoveDirection() {return bullet.getMoveDirection();}
    public void setMoveDirection(Vector3 v) { bullet.setMoveDirection(v);}

}