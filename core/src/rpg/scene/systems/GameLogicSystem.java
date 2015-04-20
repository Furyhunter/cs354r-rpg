package rpg.scene.systems;

import rpg.Diagnostics;
import rpg.scene.Node;
import rpg.scene.components.Steppable;

/**
 * A system that processes all Steppable components added to the scene.
 */
public class GameLogicSystem extends AbstractSceneSystem {

    @Override
    public void beginProcessing() {
        Diagnostics.beginTime(Diagnostics.GAMELOGIC_TOTAL_TIME);
    }

    @Override
    public void enterNode(Node n, float deltaTime) {

    }

    @Override
    public void processNode(Node n, float deltaTime) {
        Diagnostics.beginTime(Diagnostics.PROCESSCOMPONENTS_TOTAL_TIME);
        n.findComponents(c -> c instanceof Steppable)
                .forEach(s -> ((Steppable) s).step(deltaTime));
        Diagnostics.endTime(Diagnostics.PROCESSCOMPONENTS_TOTAL_TIME);
    }

    @Override
    public void exitNode(Node n, float deltaTime) {

    }

    @Override
    public void endProcessing() {
        Diagnostics.endTime(Diagnostics.GAMELOGIC_TOTAL_TIME);
    }
}
