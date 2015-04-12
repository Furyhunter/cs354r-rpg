package rpg.scene;

import rpg.scene.components.Component;
import rpg.scene.components.ReplicationComponent;
import rpg.scene.components.Transform;
import rpg.scene.systems.SceneSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Node {
    private List<Node> children = new ArrayList<>();
    private int networkID = 0;
    private String name = "";

    private List<Component> components = new ArrayList<>();

    private static int networkIDCounter = 0;

    private Node parent;
    private Scene scene;

    public Node() {
        this("");
    }

    Node(Scene scene) {
        this.scene = scene;

        this.name = name;
        networkID = networkIDCounter++;

        addComponent(new Transform());
        addComponent(new ReplicationComponent());
    }

    public Node(String name) {
        this.name = name;
        networkID = networkIDCounter++;

        addComponent(new Transform());
        addComponent(new ReplicationComponent());
    }

    /**
     * Get an unmodifiable list of the children.
     *
     * @return
     */
    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Get an unmodifiable list of the components.
     *
     * @return
     */
    public List<Component> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * Finds the first component with the type given.
     *
     * @param type class instance of component you want
     * @param <T>
     * @return a component of type T, or null.
     */
    public <T> T findComponent(Class<T> type) {
        for (Component c : components) {
            if (type.isInstance(c)) {
                return (T) c;
            }
        }
        return null;
    }

    public Component findComponent(Predicate<Component> predicate) {
        for (Component c : components) {
            if (predicate.test(c)) {
                return c;
            }
        }
        return null;
    }

    public List<Component> findComponents(Predicate<Component> predicate) {
        return components.stream().filter(predicate).collect(Collectors.toList());
    }

    public int getNumChildren() {
        return children.stream().mapToInt(Node::getNumChildren).sum() + children.size();
    }

    public String getName() {
        return name;
    }

    public void addChild(Node n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (n.getParent() != null) {
            throw new RuntimeException("node already has a parent, remove from previous parent first");
        }
        children.add(n);
        n.setParent(this);
    }

    public void addComponent(Component c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c.getParent() != null) {
            throw new RuntimeException("component already has a parent, remove from previous parent first");
        }
        components.add(c);
        c.setParent(this);
    }

    public void removeChild(Node n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (children.remove(n)) {
            n.setParent(null);
        }
    }

    public void removeComponent(Component c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (components.remove(c)) {
            c.setParent(null);
        }
    }

    /**
     * Traverses upward until it finds the root node for this tree.
     *
     * @return The root node of the tree
     */
    public Node findRoot() {
        Node current = this;
        while (true) {
            Node parent = current.getParent();
            if (parent == null) {
                break;
            }
            current = parent;
        }
        return current;
    }

    public int getNetworkID() {
        return networkID;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
        if (parent != null) {
            scene = null;
        }
    }

    /**
     * Gets the scene this node belongs to. This will traverse up the tree if it has to,
     * so make sure to store the result.
     *
     * @return the Scene this node belongs to
     */
    public Scene getScene() {
        if (parent == null) {
            return scene;
        }
        return findRoot().getScene();
    }

    public void setScene(Scene scene) {
        if (parent != null) {
            throw new RuntimeException("Only a root node may have the scene set.");
        }
        this.scene = scene;
    }

    public void process(SceneSystem system, float deltaTime) {
        system.processNode(this, deltaTime);
        components.forEach(c -> system.processComponent(c, deltaTime));
        children.forEach(n -> n.process(system, deltaTime));
    }
}
