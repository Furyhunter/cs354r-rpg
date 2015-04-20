package rpg.scene.components;

import rpg.scene.systems.InputSystem;

public interface InputEventListener {
    void processInputEvent(InputSystem.InputEvent event);
}
