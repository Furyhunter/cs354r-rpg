package rpg.scene;

import rpg.scene.components.Component;
import rpg.scene.systems.SceneSystem;

import java.util.*;

public class Scene {
    private Node root;

    private Set<Node> nodes = new HashSet<>(1024);
    private Set<Component> components = new HashSet<>(1024);

    private List<SceneSystem> systems = new ArrayList<>();

    public Scene() {
        root = new Node(this);
    }

    public Node getRoot() {
        return root;
    }

    public int getNumNodes() {
        return root.getNumChildren() + 1;
    }

    public void addSystem(SceneSystem s) {
        Objects.requireNonNull(s);

        systems.add(s);
        s.setParent(this);
    }

    public void removeSystem(SceneSystem s) {
        Objects.requireNonNull(s);

        systems.remove(s);
    }

    public <T extends SceneSystem> void removeSystem(Class<T> type) {
        Objects.requireNonNull(type);
        systems.removeIf(type::isInstance);
    }

    public <T extends SceneSystem> T findSystem(Class<T> type) {
        Objects.requireNonNull(type);
        Optional<SceneSystem> o = systems.stream().filter(type::isInstance).findFirst();
        if (o.isPresent()) {
            return (T) o.get();
        }
        return null;
    }

    public void update(float deltaTime) {
        List<SceneSystem> systemsCopy = new ArrayList<>(systems);
        Iterator<SceneSystem> itr = systemsCopy.iterator();
        while (itr.hasNext()) {
            SceneSystem s = itr.next();
            if (!systems.contains(s)) {
                itr.remove();
                continue;
            }

            s.beginProcessing();

            if (s.doesProcessNodes()) {
                root.process(s, deltaTime);
            }

            s.endProcessing();

            if (!systems.contains(s)) {
                itr.remove();
            }
        }
    }

    public void nodeAttached(Node n) {
        nodes.add(n);
        systems.forEach(s -> s.nodeAttached(n));
    }

    public void nodeDetached(Node n) {
        nodes.remove(n);
        systems.forEach(s -> s.nodeDetached(n));
    }

    public void nodeReattached(Node n) {
        systems.forEach(s -> s.nodeReattached(n));

    }

    public void componentAttached(Component c) {
        components.add(c);
        systems.forEach(s -> s.componentAttached(c));

    }

    public void componentReattached(Component c) {
        systems.forEach(s -> s.componentReattached(c));

    }

    public void componentDetached(Component c) {
        components.remove(c);
        systems.forEach(s -> s.componentDetached(c));

    }

    public Node findNode(int nodeID) {
        Optional<Node> nn = nodes.stream().filter(n -> n.getNetworkID() == nodeID).findFirst();
        if (nn.isPresent()) {
            return nn.get();
        } else {
            return null;
        }
    }

    public Component findComponent(int componentID) {
        Optional<Component> cc = components.stream().filter(c -> c.getNetworkID() == componentID).findFirst();
        if (cc.isPresent()) {
            return cc.get();
        } else {
            return null;
        }
    }
}
