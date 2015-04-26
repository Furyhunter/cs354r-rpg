package rpg.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
    private VerticalGroup chatMessageList;
    private ScrollPane chatScrollPane;

    private boolean newMessage = false;

    @Override
    public void init() {
        if (skin == null) {
            GdxAssetManagerSystem.getSingleton().getAssetManager().load("uiskin/uiskin.json", Skin.class);
            GdxAssetManagerSystem.getSingleton().getAssetManager().finishLoading();
            skin = GdxAssetManagerSystem.getSingleton().getAssetManager().get("uiskin/uiskin.json");
        }
        table.clear();
        table.setSkin(skin);
        chatEntry = new TextField("", skin);
        Table chatBox = new Table(skin);
        table.add().expand();
        table.row();
        table.add(chatBox).left().prefWidth(400).bottom();

        chatMessageList = new VerticalGroup();
        chatMessageList.left();
        chatScrollPane = new ScrollPane(chatMessageList, skin);
        chatScrollPane.setScrollBarPositions(true, false);
        chatScrollPane.setScrollingDisabled(true, false);
        chatScrollPane.setFadeScrollBars(false);
        chatBox.add(chatScrollPane).left().bottom().fillX().maxHeight(100);
        chatBox.row();
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
                            } else {
                                Log.warn(getClass().getSimpleName(), "Couldn't find PlayerInfoComponent in possessed nodes.");
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

    @Override
    public void update(float deltaTime) {
        if (newMessage) {
            chatScrollPane.setScrollPercentY(1);
            newMessage = false;
        }
    }

    public void addChatMessage(PlayerInfoComponent component, String message) {
        Log.info(getClass().getSimpleName(), component.getPlayerName() + ": " + message);
        Label messageLabel = new Label(component.getPlayerName() + ": " + message, skin);
        messageLabel.setWrap(true);
        messageLabel.setWidth(400);

        if (Math.abs(chatScrollPane.getVisualScrollPercentY()) - 1.0f < 0.00001f || chatScrollPane.getVisualScrollY() == Float.NaN) {
            chatMessageList.addActor(messageLabel);
            chatScrollPane.setScrollPercentY(1);
            chatScrollPane.layout();
            newMessage = true;
        } else {
            chatMessageList.addActor(messageLabel);
        }
    }

    public void focusChatEntry() {
        chatEntry.getStage().setKeyboardFocus(chatEntry);
    }
}
