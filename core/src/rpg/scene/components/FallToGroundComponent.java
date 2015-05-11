package rpg.scene.components;

import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.replication.Context;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.NetworkingSceneSystem;

public class FallToGroundComponent extends Component implements Steppable {

    @Replicated
    protected float velocityZ = 0;

    @Replicated
    protected Node shadowNode;

    public static final float ACCELERATION = 15;

    public FallToGroundComponent() {
        super();
    }

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Transform t = getParent().getTransform();
        if (nss.getContext() == Context.Server && shadowNode == null) {
            // Just assume the first node is the shadow node...
            shadowNode = getParent().getChildren().stream().findFirst().orElse(null);
        }

        if (t.getPosition().z > 0.0001f) {
            velocityZ -= ACCELERATION * deltaTime;

            t.translate(0, 0, velocityZ * deltaTime);

            if (shadowNode != null) {
                Vector3 pos = shadowNode.getTransform().getPosition();
                shadowNode.getTransform().setPosition(pos.cpy().set(pos.x, pos.y, -t.getWorldPosition().z * 2 + 0.005f));
            }
        } else if (t.getPosition().z < -0.0001f) {
            t.translate(0, 0, -t.getPosition().z);

            if (shadowNode != null) {
                Vector3 pos = shadowNode.getTransform().getPosition();
                shadowNode.getTransform().setPosition(pos.cpy().set(pos.x, pos.y, -t.getWorldPosition().z * 2 + 0.005f));
            }

            if (nss.getContext() == Context.Server) {
                getParent().removeComponent(this);
            }
            return;
        }
    }
}
