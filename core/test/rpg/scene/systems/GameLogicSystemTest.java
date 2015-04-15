package rpg.scene.systems;

import org.junit.Test;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.Component;
import rpg.scene.components.Steppable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GameLogicSystemTest {
    private int testValue = 0;

    public static final int EXPECTED_TEST_VALUE = 2841;

    class GameLogicComponentTestComponent extends Component implements Steppable {
        int pos = 0;

        @Override
        public void step(float deltaTime) {
            testValue = deltaTime == 1 ? EXPECTED_TEST_VALUE : 0;
            pos++;
        }
    }

    @Test
    public void testAddComponent() {
        Scene s = new Scene();
        GameLogicSystem gameLogicSystem = new GameLogicSystem();
        s.addSystem(gameLogicSystem);
        s.getRoot().addComponent(new GameLogicComponentTestComponent());
        s.update(1);

        assertEquals("The system did not update the component.", EXPECTED_TEST_VALUE, testValue);
    }

    @Test
    public void testMultiAddAndUpdateComponent() {
        Scene s = new Scene();
        GameLogicSystem gameLogicSystem = new GameLogicSystem();
        s.addSystem(gameLogicSystem);
        Node n = new Node();
        GameLogicComponentTestComponent c1 = new GameLogicComponentTestComponent();
        GameLogicComponentTestComponent c2 = new GameLogicComponentTestComponent();
        s.getRoot().addComponent(c1);
        s.getRoot().addChild(n);

        s.update(1);

        n.addComponent(c2);
        s.update(1);

        s.getRoot().removeChild(n);

        assertNull("Parent was not set to null", n.getParent());

        s.update(1);

        assertEquals("The root component was not correctly processed by the system.", 3, c1.pos);
        assertEquals("The child node's component was not correctly processed by the system.", 1, c2.pos);
    }

    @Test
    public void testAddAndRemoveSystem() {
        Scene s = new Scene();
        GameLogicSystem gameLogicSystem = new GameLogicSystem();
        s.addSystem(gameLogicSystem);
        GameLogicComponentTestComponent c1 = new GameLogicComponentTestComponent();
        s.getRoot().addComponent(c1);

        s.update(1);

        s.removeSystem(GameLogicSystem.class);

        s.update(1);

        assertEquals("The system did not process correctly.", 1, c1.pos);
    }
}