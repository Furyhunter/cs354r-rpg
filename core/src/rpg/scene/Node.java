package rpg.scene;

import rpg.scene.components.Component;
import rpg.scene.components.ReplicationComponent;
import rpg.scene.components.Transform;
import rpg.scene.systems.SceneSystem;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Node {
    private Set<Node> children = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
    private int networkID = 0;
    private String name = "";

    private List<Component> components = new ArrayList<>();

    private static int networkIDCounter = 0;

    public static final int ROOT_NODE_NETWORK_ID = -1;

    private Node parent;
    private Scene scene;

    private Transform myTransform;
    private ReplicationComponent myReplicationComponent;

    /**
     * Creates a node, without attaching the node to a parent.
     */
    public Node() {
        this("");
    }

    /**
     * Creates a node, attaching it to the given parent node, with no name.
     *
     * @param parent the parent Node
     */
    public Node(Node parent) {
        this(parent, "");
    }

    /**
     * Used by Scene when creating the root node.
     *
     * @param scene
     */
    Node(Scene scene) {
        this.scene = scene;

        networkID = ROOT_NODE_NETWORK_ID;

        myTransform = new Transform();
        myReplicationComponent = new ReplicationComponent();

        addComponent(myTransform);
        addComponent(myReplicationComponent);
    }

    /**
     * Creates a node, attaching it to the given parent node.
     *
     * @param parent the parent Node
     * @param name the name of this node
     */
    public Node(Node parent, String name) {
        Objects.requireNonNull(parent);
        this.name = name;
        networkID = networkIDCounter++;

        parent.addChild(this);
        myTransform = new Transform();
        myReplicationComponent = new ReplicationComponent();

        addComponent(myTransform);
        addComponent(myReplicationComponent);
    }

    /**
     * Creates a node without attaching the node to a parent.
     *
     * @param name the name of this node
     */
    public Node(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        networkID = networkIDCounter++;


        myTransform = new Transform();
        myReplicationComponent = new ReplicationComponent();

        addComponent(myTransform);
        addComponent(myReplicationComponent);
    }

    /**
     * Get an unmodifiable list of the children.
     *
     * @return
     */
    public Set<Node> getChildren() {
        return Collections.unmodifiableSet(children);
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

    public <T> List<T> findComponents(Class<T> type) {
        List<T> ret = new ArrayList<T>();
        components.stream().filter(type::isInstance).forEach(c -> ret.add((T) c));
        return ret;
    }

    public int getNumChildren() {
        return children.stream().mapToInt(Node::getNumChildren).sum() + children.size();
    }

    public String getName() {
        return name;
    }

    public void addChild(Node n) {
        Objects.requireNonNull(n);
        if (n.getParent() != null) {
            n.getParent().children.remove(n);
            n.setParent(this);
            children.add(n);
            getScene().nodeReattached(n);
        } else {
            n.setParent(this);
            children.add(n);
            getScene().nodeAttached(n);
        }
    }

    public void addComponent(Component c) {
        Objects.requireNonNull(c);
        if (c.getParent() != null) {
            c.getParent().components.remove(c);
            c.setParent(this);
            components.add(c);
            getScene().componentReattached(c);
        } else {
            c.setParent(this);
            components.add(c);
            getScene().componentAttached(c);
        }
    }

    public void removeChild(Node n) {
        Objects.requireNonNull(n);
        if (children.remove(n)) {
            n.setParent(null);
            getScene().nodeDetached(n);
        }
    }

    public void removeComponent(Component c) {
        Objects.requireNonNull(c);
        if (components.remove(c)) {
            c.setParent(null);
            getScene().componentDetached(c);
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
     * but the result will be memoized, so future calls will be faster. The memo will be
     * invalidated if the containment of this node changes.
     *
     * @return the Scene this node belongs to
     */
    public Scene getScene() {
        if (scene != null) {
            return scene;
        }
        if (parent == null) {
            return scene;
        }
        scene = findRoot().getScene();
        return scene;
    }

    public void setScene(Scene scene) {
        if (parent != null) {
            throw new RuntimeException("Only a root node may have the scene set.");
        }
        this.scene = scene;
    }

    public void process(SceneSystem system, float deltaTime) {
        system.enterNode(this, deltaTime);

        system.processNode(this, deltaTime);

        Set<Node> childrenCopy = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
        childrenCopy.addAll(children);

        Iterator<Node> itr = childrenCopy.iterator();
        while (itr.hasNext()) {
            Node n = itr.next();
            if (!children.contains(n)) {
                itr.remove();
                continue;
            }
            n.process(system, deltaTime);
            if (!children.contains(n)) {
                itr.remove();
                continue;
            }
        }

        system.exitNode(this, deltaTime);
    }

    public Transform getTransform() {
        return myTransform;
    }

    public ReplicationComponent getReplicationComponent() {
        return myReplicationComponent;
    }
}
