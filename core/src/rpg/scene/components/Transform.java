package rpg.scene.components;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import rpg.scene.replication.Replicated;

public class Transform extends Component {
    @Replicated
    private Vector2 position = new Vector2();
    @Replicated
    private Vector2 scale = new Vector2(1, 1);
    @Replicated
    private float rotation = 0;

    public Transform() {
        super();
    }

    /**
     * Mutate the input matrix to apply the transformation.
     *
     * @param in
     */
    public void applyTransform(Matrix3 in) {
        in.mulLeft(new Matrix3().translate(position));
        in.mulLeft(new Matrix3().rotateRad(rotation));
        in.mulLeft(new Matrix3().scale(scale));
    }

    /**
     * Apply the inverse transformation. Used to get camera transform.
     *
     * @param in
     */
    public void inverseApplyTransform(Matrix3 in) {
        in.mulLeft(new Matrix3().translate(position.cpy().scl(-1)));
        in.mulLeft(new Matrix3().rotateRad(-rotation));
        in.mulLeft(new Matrix3().scale(new Vector2(1.0f / scale.x, 1.0f / scale.y)));
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getScale() {
        return scale;
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
