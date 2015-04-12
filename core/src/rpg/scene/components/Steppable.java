package rpg.scene.components;

/**
 * An interface for classes that can be stepped by the GameLogicSystem.
 */
public interface Steppable {
    /**
     * Run an iteration of game logic for this component.
     *
     * @param deltaTime the length, in seconds, for this step.
     */
    void step(float deltaTime);
}
