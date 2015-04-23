package rpg.scene.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.esotericsoftware.minlog.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scene2DUISystem extends AbstractSceneSystem {

    private Stage stage = new Stage();

    public Stage getStage() {
        return stage;
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
        GdxAssetManagerSystem.getSingleton().getAssetManager().load("uiskin/uiskin.json", Skin.class);
        GdxAssetManagerSystem.getSingleton().getAssetManager().finishLoading();
        Skin s = GdxAssetManagerSystem.getSingleton().getAssetManager().get("uiskin/uiskin.json");
        Table t = new Table(s);
        TextButton b = new TextButton("hello world", s);
        t.setFillParent(true);
        t.setDebug(true);
        b.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Log.info("hello world");
            }
        });
        t.add(b);
        t.bottom().right();

        stage.setViewport(new FitViewport(800, 600));
        stage.addActor(t);
        InputProcessor current = Gdx.input.getInputProcessor();
        InputProcessor ip = new WrapperInputProcessor(current, stage);
        Gdx.input.setInputProcessor(ip);
    }

    @Override
    public void endProcessing() {
        stage.act(Gdx.graphics.getDeltaTime());
        stage.getViewport().setWorldWidth(600 * ((float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight()));
        stage.getViewport().setWorldHeight(600);
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        stage.draw();
    }

    @Override
    public boolean doesProcessNodes() {
        return false;
    }
}
