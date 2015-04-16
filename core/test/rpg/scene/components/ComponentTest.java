package rpg.scene.components;

import org.junit.Test;
import rpg.scene.Node;
import rpg.scene.Scene;

import static org.junit.Assert.assertNotNull;

public class ComponentTest {

    @Test
    public void testComponentAttach() {
        Scene s = new Scene();
        Node n = new Node(s.getRoot());
        Component c = new Component() {
        };
        n.addComponent(c);
        Component cc = n.findComponent(c.getClass());
        assertNotNull(cc);
    }
}