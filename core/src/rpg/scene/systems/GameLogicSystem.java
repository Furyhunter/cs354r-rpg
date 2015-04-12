package rpg.scene.systems;

import rpg.scene.Node;
import rpg.scene.components.Component;
import rpg.scene.components.Steppable;

/**
 * A system that processes all Steppable components added to the scene.
 */
public class GameLogicSystem extends AbstractSceneSystem {

    @Override
    public void processComponent(Component c, float deltaTime) {
        // This could probably be optimized.
        if (c instanceof Steppable) {
            Steppable s = (Steppable) c;
            s.step(deltaTime);
        }
    }

    @Override
    public void processNode(Node n, float deltaTime) {

    }
}
