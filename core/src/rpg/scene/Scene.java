package rpg.scene;

public class Scene {
    private Node root = new Node();

    public Node getRoot() {
        return root;
    }

    public int getNumNodes() {
        return root.getNumChildren() + 1;
    }
}
