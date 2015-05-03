package rpg.scene.components;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import rpg.client.KryoClientSceneSystem;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPC;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.RendererSceneSystem;

import java.util.LinkedList;
import java.util.Objects;

public class Transform extends Component {
    @Replicated
    protected Vector3 position = new Vector3();
    @Replicated
    protected Vector3 scale = new Vector3(1, 1, 1);
    @Replicated
    protected Quaternion rotation = new Quaternion();

    private Vector3 worldPosition = new Vector3();
    private Vector3 worldScale = new Vector3();
    private Quaternion worldRotation = new Quaternion();

    private boolean worldTransformsDirty = true;

    public Transform() {
        super();
    }

    /**
     * Mutate the input matrix to apply the transformation.
     *
     * @param in
     */
    public void applyTransform(Matrix4 in) {
        in.mulLeft(new Matrix4().scale(scale.x, scale.y, scale.z));
        in.mulLeft(new Matrix4().rotate(rotation));
        in.mulLeft(new Matrix4().translate(position));
    }

    /**
     * Apply the inverse transformation. Used to get camera transform.
     *
     * @param in
     */
    public void inverseApplyTransform(Matrix4 in) {
        in.mulLeft(new Matrix4().translate(position.cpy().scl(-1)));
        in.mulLeft(new Matrix4().rotate(new Quaternion(rotation).conjugate()));
        in.mulLeft(new Matrix4().scale(1.0f / scale.x, 1.0f / scale.y, 1.0f / scale.z));
    }

    private void reevaluateWorldTransform() {
        LinkedList<Transform> transforms = new LinkedList<>();
        Node cur = getParent();
        while (cur != null) {
            transforms.addLast(cur.getTransform());
            cur = cur.getParent();
        }
        // We don't want the root node transform so we'll drop it.
        //transforms.removeFirst();

        worldPosition.setZero();
        worldScale.set(1, 1, 1);
        worldRotation = new Quaternion();
        transforms.forEach(t -> {
            // Apply parent scale
            worldScale.scl(t.scale);
            // Apply rotation (left multiply parent * this)
            worldRotation.mulLeft(t.rotation);

            // Position is a bit trickier
            // Scale translation by parent scale
            worldPosition.add(t.position);
            worldPosition.scl(t.scale);
            // parentRotation * thisTranslation
            worldPosition.mul(t.rotation);
            // thisTranslation += parentTranslation
        });
        worldTransformsDirty = false;
    }

    private void setDirty() {
        worldTransformsDirty = true;
        if (getParent() != null) {
            getParent().getChildren().stream().map(Node::getTransform).forEach(Transform::setDirty);
        }
    }

    public Vector3 getWorldPosition() {
        if (worldTransformsDirty) reevaluateWorldTransform();
        return worldPosition;
    }

    public Vector3 getWorldScale() {
        if (worldTransformsDirty) reevaluateWorldTransform();
        return worldScale;
    }

    public Quaternion getWorldRotation() {
        if (worldTransformsDirty) reevaluateWorldTransform();
        return worldRotation;
    }

    /**
     * Gets the position vector. DO NOT MODIFY!
     *
     * @return position vector
     */
    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = new Vector3(position);
        setDirty();
    }

    /**
     * Gets the scale vector. DO NOT MODIFY!
     * @return position vector
     */
    public Vector3 getScale() {
        return scale;
    }

    public void setScale(Vector3 scale) {
        this.scale = scale;
        setDirty();
    }

    /**
     * Gets the rotation quaternion. DO NOT MODIFY!
     * @return position vector
     */
    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = new Quaternion(rotation);
        setDirty();
    }

    public void translate(Vector3 translation) {
        Objects.requireNonNull(translation);
        this.position = this.position.cpy().add(translation);
    }

    public void translate(float x, float y, float z) {
        position = position.cpy().add(x, y, z);
        setDirty();
    }

    public void rotate(Quaternion quaternion) {
        Objects.requireNonNull(quaternion);
        this.rotation = this.rotation.cpy().mulLeft(quaternion);
        setDirty();
    }

    public void rotate(Vector3 axis, float angleDegrees) {
        Objects.requireNonNull(axis);
        this.rotation = this.rotation.cpy().mulLeft(new Quaternion(axis, angleDegrees));
        setDirty();
    }

    public void scale(Vector3 scale) {
        Objects.requireNonNull(scale);
        this.scale = this.scale.cpy().scl(scale);
        setDirty();
    }

    public void scale(float s) {
        scale = scale.cpy().scl(s);
    }

    @RPC(target = RPC.Target.Client)
    public void possessNode() {
        Scene s = getParent().getScene();
        KryoClientSceneSystem nss = s.findSystem(KryoClientSceneSystem.class);
        if (nss == null || nss.getContext() == Context.Client) {
            Log.info(getClass().getSimpleName(), "Possessing node " + getParent().getNetworkID());
            RendererSceneSystem r = s.findSystem(RendererSceneSystem.class);
            getParent().setPossessed(true);
            if (nss != null) {
                nss.addPossessedNode(getParent());
            }
            if (r != null) {
                r.setViewTarget(getParent());
            }
        }
    }
}
