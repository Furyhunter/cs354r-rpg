package rpg.scene.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import rpg.scene.Node;
import rpg.scene.components.Component;
import rpg.scene.components.InputEventListener;

import java.util.Comparator;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InputSystem extends AbstractSceneSystem implements InputProcessor {

    private Queue<InputEvent> inputQueue = new ConcurrentLinkedQueue<>();

    private Set<Component> listeners = new TreeSet<>(Comparator.comparingInt(Component::getNetworkID));

    private boolean processorSet = false;

    public static class InputEvent {
        public EventType getType() {
            return type;
        }

        public void setType(EventType type) {
            this.type = type;
        }

        public int getButton() {
            return button;
        }

        public void setButton(int button) {
            this.button = button;
        }

        public char getCharacter() {
            return character;
        }

        public void setCharacter(char character) {
            this.character = character;
        }

        public Vector2 getScreenPosition() {
            return screenPosition;
        }

        public void setScreenPosition(Vector2 screenPosition) {
            this.screenPosition = screenPosition;
        }

        private EventType type;
        private int button;
        private char character;

        private Vector2 screenPosition;
    }

    public enum EventType {
        KeyDown,
        KeyUp,
        KeyTyped,
        MouseButtonPressed,
        MouseButtonReleased,
        MouseDragged,
        MouseMoved,
        MouseWheelScrolled,
    }

    @Override
    public void beginProcessing() {
        if (!processorSet) {
            Gdx.input.setInputProcessor(this);
            processorSet = true;
        }
    }

    @Override
    public void endProcessing() {
        while (!inputQueue.isEmpty()) {
            InputEvent event = inputQueue.remove();
            if (event != null) {
                listeners.forEach(c -> ((InputEventListener) c).processInputEvent(event));
            }
        }
    }

    @Override
    public void processNode(Node n, float deltaTime) {
    }

    @Override
    public boolean doesProcessNodes() {
        return false;
    }

    @Override
    public void componentAttached(Component c) {
        if (c instanceof InputEventListener) {
            listeners.add(c);
        }
    }

    @Override
    public void componentDetached(Component c) {
        if (c instanceof InputEventListener) {
            listeners.remove(c);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        InputEvent evt = new InputEvent();
        evt.type = EventType.KeyDown;
        evt.button = keycode;
        inputQueue.add(evt);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        InputEvent evt = new InputEvent();
        evt.type = EventType.KeyUp;
        evt.button = keycode;
        inputQueue.add(evt);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        InputEvent evt = new InputEvent();
        evt.type = EventType.KeyTyped;
        evt.character = character;
        inputQueue.add(evt);
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        InputEvent evt = new InputEvent();
        evt.type = EventType.MouseButtonPressed;
        evt.button = button;
        evt.screenPosition = new Vector2(screenX, screenY);
        inputQueue.add(evt);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        InputEvent evt = new InputEvent();
        evt.type = EventType.MouseButtonReleased;
        evt.button = button;
        evt.screenPosition = new Vector2(screenX, screenY);
        inputQueue.add(evt);
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        InputEvent evt = new InputEvent();
        evt.type = EventType.MouseDragged;
        evt.screenPosition = new Vector2(screenX, screenY);
        inputQueue.add(evt);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        InputEvent evt = new InputEvent();
        evt.type = EventType.MouseMoved;
        evt.screenPosition = new Vector2(screenX, screenY);
        inputQueue.add(evt);
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        InputEvent evt = new InputEvent();
        evt.type = EventType.MouseWheelScrolled;
        evt.button = amount;
        inputQueue.add(evt);
        return true;
    }
}
