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
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     */
    void beginProcessing();

    /**
     * Called when entering a node during scene processing.
     * <p>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     *
     * @param n         the node being entered
     * @param deltaTime the time in seconds for this update
     */
    void enterNode(Node n, float deltaTime);

    /**
     * Called to process a node in the scene.
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     *
     * @param n         the node to process
     * @param deltaTime The time, in seconds, since the last call
     */
    void processNode(Node n, float deltaTime);

    /**
     * Called when exiting a node during scene processing.
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     *
     * @param n the node being entered
     * @param deltaTime the time in seconds for this update
     */
    void exitNode(Node n, float deltaTime);

    void setParent(Scene parent);

    Scene getParent();

    /**
     * Called after all nodes have been processed.
     */
    void endProcessing();

    /**
     * Called when a node has been attached somewhere in the scene. It will not be called
     * if the node is simply moving attachments; see {@link #nodeReattached(Node)} instead.
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     * @param n the node that was attached.
     */
    void nodeAttached(Node n);

    /**
     * Called when a node has been reattached somewhere in the scene. This happens when a node
     * has been added ({@link Node#addChild(Node)}) when it already has a parent node. This is
     * the only method that will be called in this scenario.
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     * @param n the node that was reattached.
     */
    void nodeReattached(Node n);

    /**
     * Called when a node has been detached somewhere in the scene. This happens when a node
     * has been removed with {@link Node#removeChild(Node)}.
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     * @param n the node that was detached.
     */
    void nodeDetached(Node n);

    /**
     * Called when a component is attached to a node.
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     * @param c the component that was attached.
     */
    void componentAttached(Component c);

    /**
     * Called when a component is reattached to another node.
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     * @param c the component that was reattached.
     */
    void componentReattached(Component c);

    /**
     * Called when a component is detached from a node.
     * <p/>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     * @param c the component that was detached.
     */
    void componentDetached(Component c);

    /**
     * Whether or not to call {@link #enterNode(Node, float)} {@link #exitNode(Node, float)} and
     * {@link #processNode(Node, float)}.
     * <p>
     * For concurrent access safety, do not remove or add SceneSystems in this method.
     *
     * @return true if this system processes nodes, false otherwise.
     */
    boolean doesProcessNodes();
}
