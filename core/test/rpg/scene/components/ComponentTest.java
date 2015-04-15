package rpg.scene.components;

import org.junit.Test;
import rpg.scene.Node;

import static org.junit.Assert.assertNotNull;

public class ComponentTest {

    @Test
    public void testComponentAttach() {
        Node n = new Node();
        Component c = new Component() {
        };
        n.addComponent(c);
        Component cc = n.findComponent(c.getClass());
        assertNotNull(cc);
    }
}