package rpg.scene;

import rpg.scene.components.Component;
import rpg.scene.systems.SceneSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Scene {
    private Node root;

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
        if (o.get() != null) {
            return (T) o.get();
        }
        return null;
    }

    public void update(float deltaTime) {
        systems.forEach(s -> {
            s.beginProcessing();
            root.process(s, deltaTime);
            s.endProcessing();
        });
    }

    public void nodeAttached(Node n) {
        systems.forEach(s -> s.nodeAttached(n));
    }

    public void nodeDetached(Node n) {
        systems.forEach(s -> s.nodeDetached(n));
    }

    public void nodeReattached(Node n) {
        systems.forEach(s -> s.nodeReattached(n));

    }

    public void componentAttached(Component c) {
        systems.forEach(s -> s.componentAttached(c));

    }

    public void componentReattached(Component c) {
        systems.forEach(s -> s.componentReattached(c));

    }

    public void componentDetached(Component c) {
        systems.forEach(s -> s.componentDetached(c));

    }
}
