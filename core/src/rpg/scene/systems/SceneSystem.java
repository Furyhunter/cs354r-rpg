package rpg.scene.systems;

import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.Component;

/**
 * An interface for classes that process events in a system.
 */
public interface SceneSystem {
    /**
     * Called to process a component in the scene.
     *
     * @param deltaTime The time, in seconds, since the last call
     */
    void processComponent(Component c, float deltaTime);

    /**
     * Called to process a node in the scene.
     *
     * @param n         the node to process
     * @param deltaTime The time, in seconds, since the last call
     */
    void processNode(Node n, float deltaTime);

    void setParent(Scene parent);

    Scene getParent();
}
