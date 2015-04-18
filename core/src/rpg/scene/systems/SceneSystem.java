package rpg.scene.systems;

import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.Component;

/**
 * An interface for classes that process events in a system.
 */
public interface SceneSystem {

    /**
     * Called at the start of graph processing.
     */
    void beginProcessing();

    void enterNode(Node n, float deltaTime);

    /**
     * Called to process a node in the scene.
     *
     * @param n         the node to process
     * @param deltaTime The time, in seconds, since the last call
     */
    void processNode(Node n, float deltaTime);

    void exitNode(Node n, float deltaTime);

    void setParent(Scene parent);

    Scene getParent();

    /**
     * Called after all nodes have been processed.
     */
    void endProcessing();


    void nodeAttached(Node n);

    void nodeReattached(Node n);

    void nodeDetached(Node n);

    void componentAttached(Component c);

    void componentReattached(Component c);

    void componentDetached(Component c);
}
