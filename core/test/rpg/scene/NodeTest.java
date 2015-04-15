package rpg.scene;

import org.junit.Test;
import rpg.scene.components.ReplicationComponent;
import rpg.scene.components.Transform;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NodeTest {

    @Test
    public void testGetNumChildren() throws Exception {
        Node n = new Node();
        IntStream.range(0, 10).forEach(a -> n.addChild(new Node(Integer.toString(a))));
        assertEquals(10, n.getNumChildren());

        n.getChildren().stream().forEach(node -> IntStream.range(0, 10).forEach(a -> node.addChild(new Node(Integer.toString(a)))));
        assertEquals(110, n.getNumChildren());
    }

    @Test
    public void testUniqueNetworkIDs() throws Exception {
        Node n = new Node();
        IntStream.range(0, 100000).forEach(a -> n.addChild(new Node(Integer.toString(a))));
        Set<Integer> networkIDs = new TreeSet<>();
        n.getChildren().stream().forEach(node -> networkIDs.add(node.getNetworkID()));
        networkIDs.add(n.getNetworkID());
        assertEquals(100001, networkIDs.size());
    }

    @Test
    public void testNewNodeHasDefaultComponents() {
        // Transform and ReplicationComponent are required
        Node n = new Node();
        assertNotNull(n.findComponent(Transform.class));
        assertNotNull(n.findComponent(ReplicationComponent.class));

    }
}