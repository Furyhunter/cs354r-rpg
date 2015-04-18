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
        Scene s = new Scene();
        Node n = new Node(s.getRoot());
        IntStream.range(0, 10).forEach(a -> n.addChild(new Node(n, Integer.toString(a))));
        assertEquals(10, n.getNumChildren());

        n.getChildren().stream().forEach(node -> IntStream.range(0, 10).forEach(a -> new Node(node, Integer.toString(a))));
        assertEquals(110, n.getNumChildren());
    }

    @Test
    public void testUniqueNetworkIDs() throws Exception {
        Scene s = new Scene();
        Node n = new Node(s.getRoot());
        IntStream.range(0, 1000000).forEach(a -> new Node(n, Integer.toString(a)));
        Set<Integer> networkIDs = new TreeSet<>();
        n.getChildren().stream().forEach(node -> networkIDs.add(node.getNetworkID()));
        networkIDs.add(n.getNetworkID());
        assertEquals(1000001, networkIDs.size());
    }

    @Test
    public void testNewNodeHasDefaultComponents() {
        // Transform and ReplicationComponent are required
        Scene s = new Scene();
        Node n = new Node(s.getRoot());
        assertNotNull(n.findComponent(Transform.class));
        assertNotNull(n.findComponent(ReplicationComponent.class));

    }
}