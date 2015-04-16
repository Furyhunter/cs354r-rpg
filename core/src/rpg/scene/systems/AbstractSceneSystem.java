package rpg.scene.systems;

import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.Component;

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

    @Override
    public void nodeAttached(Node n) {

    }

    @Override
    public void nodeDetached(Node n) {

    }

    @Override
    public void nodeReattached(Node n) {

    }

    @Override
    public void componentAttached(Component c) {

    }

    @Override
    public void componentDetached(Component c) {

    }

    @Override
    public void componentReattached(Component c) {

    }
}
