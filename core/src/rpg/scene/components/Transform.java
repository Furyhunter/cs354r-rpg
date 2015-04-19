package rpg.scene.components;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import rpg.scene.Scene;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPC;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.NetworkingSceneSystem;
import rpg.scene.systems.RendererSceneSystem;

public class Transform extends Component {
    @Replicated
    protected Vector3 position = new Vector3();
    @Replicated
    protected Vector3 scale = new Vector3(1, 1, 1);
    @Replicated
    protected Quaternion rotation = new Quaternion();

    public Transform() {
        super();
    }

    /**
     * Mutate the input matrix to apply the transformation.
     *
     * @param in
     */
    public void applyTransform(Matrix4 in) {
        in.mulLeft(new Matrix4().setToScaling(scale));
        in.mulLeft(new Matrix4().rotate(rotation));
        in.mulLeft(new Matrix4().translate(position));
    }

    /**
     * Apply the inverse transformation. Used to get camera transform.
     *
     * @param in
     */
    public void inverseApplyTransform(Matrix4 in) {
        in.mulLeft(new Matrix4().scale(1.0f / scale.x, 1.0f / scale.y, 1.0f / scale.z));
        in.mulLeft(new Matrix4().rotate(rotation.conjugate()));
        in.mulLeft(new Matrix4().translate(position.cpy().scl(-1)));
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = new Vector3(position);
    }

    public Vector3 getScale() {
        return scale;
    }

    public void setScale(Vector3 scale) {
        this.scale = scale;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = new Quaternion(rotation);
    }

    @RPC(target = RPC.Target.Client)
    public void possessNode() {
        Scene s = getParent().getScene();
        NetworkingSceneSystem nss = s.findSystem(NetworkingSceneSystem.class);
        if (nss == null || nss.getContext() == Context.Client) {
            Log.info(getClass().getSimpleName(), "Possessing node " + getParent().getNetworkID());
            RendererSceneSystem r = s.findSystem(RendererSceneSystem.class);
            if (r != null) {
                r.setViewTarget(getParent());
            }
        }
    }
}
