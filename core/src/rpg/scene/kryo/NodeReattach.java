package rpg.scene.kryo;

public class NodeReattach {
    public int nodeID;
    public int parentID;
    public int depth;

    @Override
    public String toString() {
        return String.format("NodeReattach { %d, %d }", nodeID, parentID);
    }

    public int getNodeID() {
        return nodeID;
    }

    public int getParentID() {
        return parentID;
    }

    public int getDepth() {
        return depth;
    }
}
