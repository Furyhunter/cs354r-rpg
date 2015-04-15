package rpg.scene.systems;

import rpg.scene.Node;
import rpg.scene.components.Steppable;

/**
 * A system that processes all Steppable components added to the scene.
 */
public class GameLogicSystem extends AbstractSceneSystem {

    @Override
    public void enterNode(Node n, float deltaTime) {

    }

    @Override
    public void processNode(Node n, float deltaTime) {
        n.findComponents(c -> c instanceof Steppable)
                .forEach(s -> ((Steppable) s).step(deltaTime));
    }

    @Override
    public void exitNode(Node n, float deltaTime) {

    }
}
