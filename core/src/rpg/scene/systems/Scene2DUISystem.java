package rpg.scene.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.esotericsoftware.minlog.Log;
import rpg.ui.UIScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scene2DUISystem extends AbstractSceneSystem {

    private Stage stage = new Stage();

    private UIScreen screen;
    private UIScreen newScreen;
    private boolean pendingTransition;

    public Stage getStage() {
        return stage;
    }

    public UIScreen getScreen() {
        return screen;
    }

    public static class WrapperInputProcessor implements InputProcessor {
        List<InputProcessor> processors = new ArrayList<>();

        public WrapperInputProcessor(InputProcessor... wrappedProcessors) {
            Collections.addAll(processors, wrappedProcessors);
        }

        @Override
        public boolean keyDown(int keycode) {
            for (InputProcessor processor : processors) {
                if (processor.keyDown(keycode)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            for (InputProcessor processor : processors) {
                if (processor.keyUp(keycode)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            for (InputProcessor processor : processors) {
                if (processor.keyTyped(character)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            for (InputProcessor processor : processors) {
                if (processor.touchDown(screenX, screenY, pointer, button)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            for (InputProcessor processor : processors) {
                if (processor.touchUp(screenX, screenY, pointer, button)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            for (InputProcessor processor : processors) {
                if (processor.touchDragged(screenX, screenY, pointer)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            for (InputProcessor processor : processors) {
                if (processor.mouseMoved(screenX, screenY)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean scrolled(int amount) {
            for (InputProcessor processor : processors) {
                if (processor.scrolled(amount)) {
                    return true;
                }
            }
            return false;
        }
    }

    public Scene2DUISystem() {
        stage.setViewport(new FitViewport(800, 600));
        InputProcessor current = Gdx.input.getInputProcessor();
        InputProcessor ip = new WrapperInputProcessor(stage, current);
        Gdx.input.setInputProcessor(ip);
    }

    @Override
    public void endProcessing() {
        if (pendingTransition) {
            Log.info(getClass().getSimpleName(), "Changing screens.");
            if (screen != null) {
                screen.leave();
                screen.setScene(null);
            }
            screen = newScreen;
            newScreen = null;
            if (screen != null && !screen.isInitialized()) {
                screen.setScene(getParent());
                screen.init();
            }
            if (screen != null) {
                stage.clear();
                stage.addActor(screen.getTable());
                screen.getTable().setFillParent(true);
                screen.start();
            }
            pendingTransition = false;
        }

        if (screen != null) {
            screen.update(Gdx.graphics.getRawDeltaTime());
            stage.act(Gdx.graphics.getRawDeltaTime());
            stage.getViewport().setWorldWidth(600 * ((float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight()));
            stage.getViewport().setWorldHeight(600);
            stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            stage.draw();
        }
    }

    @Override
    public boolean doesProcessNodes() {
        return false;
    }

    public void setScreen(UIScreen screen) {
        newScreen = screen;
        pendingTransition = true;
    }
}
