package rpg.scene;

import rpg.scene.components.Component;
import rpg.scene.components.Transform;
import rpg.scene.systems.SceneSystem;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Node {
    private Set<Node> children = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
    private int networkID = 0;

    private List<Component> components = new ArrayList<>();

    private static int networkIDCounter = 0;
    private static int localNetworkIDCounter = Integer.MIN_VALUE;

    public static final int ROOT_NODE_NETWORK_ID = -1;

    private Node parent;
    private Scene scene;

    private Transform myTransform;

    private boolean defaultComponentsAttached = false;

    private boolean staticReplicant = false;

    private boolean possessed = false;

    /**
     * Used by Scene when creating the root node.
     *
     * @param scene
     */
    Node(Scene scene) {
        this.scene = scene;

        networkID = ROOT_NODE_NETWORK_ID;

        addDefaultComponents();
    }

    /**
     * Creates a node, attaching it to the given parent node.
     *
     * @param parent the parent Node
     */
    public Node(Node parent) {
        this();
        Objects.requireNonNull(parent);

        parent.addChild(this);
    }

    /**
     * Creates a node without attaching the node to a parent.
     */
    public Node() {
        networkID = networkIDCounter++;
    }

    /**
     * Create a node, explicitly setting its network ID. If you use this, you better be damn sure
     * the node has a unique network ID, because <b>bad things will happen otherwise.</b>
     *
     * @param networkID the network ID to use.
     */
    public Node(int networkID) {
        this(networkID, true);
    }

    /**
     * Create a node, optionally preventing default component creation, and with the specified network ID.
     * <p>
     * You are expected to add your own {@link Transform} and call {@link #setTransform(Transform)} to it.
     * If you don't, <b>bad things will happen.</b>
     *
     * @param networkID      the network ID to use.
     * @param createDefaults true to create default components when attached to a parent.
     */
    public Node(int networkID, boolean createDefaults) {
        this.networkID = networkID;

        defaultComponentsAttached = !createDefaults;
    }

    public static Node createLocalNode() {
        return createLocalNode(true);
    }

    public static Node createLocalNode(boolean createDefaults) {
        return new Node(localNetworkIDCounter++, createDefaults);
    }

    private void addDefaultComponents() {
        if (networkID < 0) {
            myTransform = Component.createLocalComponent(Transform.class);
        } else {
            myTransform = new Transform();
        }

        addComponent(myTransform);
        defaultComponentsAttached = true;
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

            n.getComponents().forEach(c -> getScene().componentAttached(c));
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
            if (!defaultComponentsAttached) {
                addDefaultComponents();
            }
        }
    }

    /**
     * Get the depth in the tree of this node. A depth of 0 indicates root.
     *
     * @return
     */
    public int getDepth() {
        Node n = this;
        int depthCounter = -1;
        while (n != null) {
            depthCounter++;
            n = n.getParent();
        }
        return depthCounter;
    }

    public void removeFromParent() {
        getParent().removeChild(this);
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

    public void setTransform(Transform t) {
        Objects.requireNonNull(t);
        myTransform = t;
    }

    public boolean isReplicated() {
        return networkID >= 0;
    }

    public boolean isPossessed() {
        return possessed;
    }

    public void setPossessed(boolean possessed) {
        this.possessed = possessed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return networkID == node.networkID;

    }

    @Override
    public int hashCode() {
        return networkID;
    }

    /**
     * Whether or not this node is "static replicant". A node with this true means that
     * once it becomes relevant for a player, it will remain relevant for its lifespan
     * (until it is detached from the scene).
     *
     * @return true if this node is forever relevant after it becomes relevant.
     */
    public boolean isStaticReplicant() {
        return staticReplicant;
    }

    public void setStaticReplicant(boolean staticReplicant) {
        this.staticReplicant = staticReplicant;
    }
}
