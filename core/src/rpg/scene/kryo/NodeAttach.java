package rpg.scene.kryo;

public class NodeAttach {
    public int nodeID;
    public int parentID;
    public int depth;

    @Override
    public String toString() {
        return String.format("NodeAttach { %d, %d, %d }", nodeID, parentID, depth);
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
