package rpg.scene;

import rpg.scene.components.Component;
import rpg.scene.components.ReplicationComponent;
import rpg.scene.components.Transform;

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

    public Node() {
        this("");
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
        children.add(n);
    }

    public void addComponent(Component c) {
        if (c == null) {
            throw new NullPointerException();
        }
        components.add(c);
    }

    public int getNetworkID() {
        return networkID;
    }
}
