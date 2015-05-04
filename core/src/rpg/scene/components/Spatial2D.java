package rpg.scene.components;

import com.badlogic.gdx.math.Rectangle;

/**
 * An interface for describing a 2-dimensional bounding box shape.
 * <p>
 * Components that take up space should implement this interface so that broad-phase querying can be used on
 * more than just the location of the node's Transform.
 */
public interface Spatial2D {
    /**
     * @return a rectangle describing this spatial, positioned in local space of the node.
     */
    Rectangle getRectangle();
}
