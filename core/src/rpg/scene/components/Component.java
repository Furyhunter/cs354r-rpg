package rpg.scene.components;

import rpg.scene.Node;

public abstract class Component {
    private Node parent;
    private int networkID = 0;

    private static int networkIDCounter = 0;

    public Component() {
        networkID = networkIDCounter++;
    }

    public void sendRPC(String rpcName) {
        // TODO handle RPC code
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node n) {
        parent = n;
    }
}
