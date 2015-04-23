package rpg.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.esotericsoftware.minlog.Log;
import rpg.client.KryoClientSceneSystem;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.PlayerInfoComponent;
import rpg.scene.systems.GdxAssetManagerSystem;

import java.util.List;

public class InGameUIScreen extends UIScreen {

    private Skin skin;

    private TextField chatEntry;

    @Override
    public void init() {
        if (skin == null) {
            GdxAssetManagerSystem.getSingleton().getAssetManager().load("uiskin/uiskin.json", Skin.class);
            GdxAssetManagerSystem.getSingleton().getAssetManager().finishLoading();
            skin = GdxAssetManagerSystem.getSingleton().getAssetManager().get("uiskin/uiskin.json");
        }
        table.clear();
        table.setSkin(skin);
        table.setDebug(true);
        chatEntry = new TextField("", skin);
        Table chatBox = new Table(skin);
        table.add().expand();
        table.row();
        table.add(chatBox).left().prefWidth(400);
        chatBox.add(chatEntry).prefWidth(400);

        chatEntry.addListener(e -> {
            if (e instanceof InputEvent) {
                InputEvent inputEvent = (InputEvent) e;
                if (inputEvent.getType() == InputEvent.Type.touchDown) {
                    e.cancel();
                    inputEvent.getStage().unfocus(e.getTarget());
                    return false;
                }
                if (inputEvent.getType() == InputEvent.Type.keyDown) {
                    if (inputEvent.getKeyCode() == Input.Keys.ESCAPE) {
                        e.cancel();
                        inputEvent.getStage().unfocus(e.getTarget());
                        return true;
                    }
                    if (inputEvent.getKeyCode() == Input.Keys.ENTER) {
                        inputEvent.getStage().unfocus(e.getTarget());

                        Scene s = getScene();
                        KryoClientSceneSystem c = s.findSystem(KryoClientSceneSystem.class);
                        List<Node> possessedNodes = c.getPossessedNodes();
                        for (Node n : possessedNodes) {
                            PlayerInfoComponent p = n.findComponent(PlayerInfoComponent.class);
                            if (p != null) {
                                p.sendRPC("sendChatMessage", chatEntry.getText());
                            }
                        }
                        chatEntry.setText("");
                        return true;
                    }
                }
            }
            return true;
        });
    }

    public void addChatMessage(PlayerInfoComponent component, String message) {
        Log.info(getClass().getSimpleName(), component.getPlayerName() + ": " + message);
    }

    public void focusChatEntry() {
        chatEntry.getStage().setKeyboardFocus(chatEntry);
    }
}
