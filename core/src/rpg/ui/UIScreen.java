package rpg.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import rpg.scene.Scene;

public class UIScreen {
    protected Table table = new Table();
    private boolean initialized = false;
    private Scene scene;

    public void init() {

    }

    public void start() {

    }

    public void update(float deltaTime) {

    }

    public void leave() {

    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
