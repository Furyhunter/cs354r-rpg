package rpg.scene.kryo;

public class NodeAttach {
    public int nodeID;
    public int parentID;

    @Override
    public String toString() {
        return String.format("NodeAttach { %d, %d }", nodeID, parentID);
    }
}
