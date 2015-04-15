package rpg.scene.systems;

import rpg.scene.Node;
import rpg.scene.Scene;

public abstract class AbstractSceneSystem implements SceneSystem {
    private Scene parent;

    @Override
    public void beginProcessing() {

    }

    @Override
    public void enterNode(Node n, float deltaTime) {

    }

    @Override
    public void exitNode(Node n, float deltaTime) {

    }

    @Override
    public Scene getParent() {
        return parent;
    }

    @Override
    public void setParent(Scene parent) {
        this.parent = parent;
    }

    @Override
    public void endProcessing() {

    }
}
