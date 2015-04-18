package rpg.scene.kryo;

public class NodeReattach {
    public int nodeID;
    public int parentID;

    @Override
    public String toString() {
        return String.format("NodeReattach { %d, %d }", nodeID, parentID);
    }
}
