package rpg.scene.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.replication.Context;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.NetworkingSceneSystem;

/**
 * Created by Corin Hill on 5/13/15.
 */
public class ArcToGroundComponent extends Component implements Steppable {
    @Replicated
    protected float velocityX = MathUtils.random(-2f,2f);
    @Replicated
    protected float velocityY = MathUtils.random(-2f,2f);
    @Replicated
    protected float velocityZ = MathUtils.random(3f,6f);

    public static float ACCELERATION = 15;

    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Transform t = getParent().getTransform();

        if (t.getPosition().z > 0.001f) {
            velocityZ -= ACCELERATION *deltaTime;

            t.translate(velocityX* deltaTime,velocityY* deltaTime,velocityZ* deltaTime);

        } else if (t.getPosition().z < 0.001f){
            t.translate(0, 0, -t.getPosition().z);
            // Still clipping into ground... so "bounce" back up a bit? it's better a bit
            t.translate(0, 0, 0.3f);

            if (nss.getContext() == Context.Server) {
                getParent().removeComponent(this);
            }
        }


    }
}
