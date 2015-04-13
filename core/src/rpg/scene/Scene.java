package rpg.scene;

import rpg.scene.systems.SceneSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Scene {
    private Node root;

    private List<SceneSystem> systems = new ArrayList<SceneSystem>();

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

    public void update(float deltaTime) {
        systems.forEach(s -> {
            s.beginProcessing();
            root.process(s, deltaTime);
            s.endProcessing();
        });
    }
}
