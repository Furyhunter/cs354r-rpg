package rpg.scene.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Scene;
import rpg.scene.replication.Context;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.NetworkingSceneSystem;

public class PansUpComponent extends Component implements Steppable {
    @Replicated
    protected boolean predictive = true;

    @Replicated
    protected float rotateRate = MathUtils.random(360) - 180;

    @Override
    public void step(float deltaTime) {
        Scene s = getParent().getScene();
        NetworkingSceneSystem n = s.findSystem(NetworkingSceneSystem.class);
        if (predictive || (n != null && n.getContext() == Context.Server) || n == null) {
            Transform t = getParent().getTransform();
            t.setPosition(new Vector3(t.getPosition()).add(0, deltaTime / 10, 0));
            t.setRotation(new Quaternion(t.getRotation()).mulLeft(new Quaternion(Vector3.Z, deltaTime * rotateRate)));
        }
    }
}
