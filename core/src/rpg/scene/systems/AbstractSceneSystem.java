package rpg.scene.systems;

import rpg.scene.Scene;

public abstract class AbstractSceneSystem implements SceneSystem {
    private Scene parent;

    @Override
    public Scene getParent() {
        return parent;
    }

    @Override
    public void setParent(Scene parent) {
        this.parent = parent;
    }
}
