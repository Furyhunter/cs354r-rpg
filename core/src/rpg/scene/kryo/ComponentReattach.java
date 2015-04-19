package rpg.scene.kryo;

public class ComponentReattach {
    public int componentID;
    public int parentNodeID;

    @Override
    public String toString() {
        return String.format("ComponentReattach { %d, %d }", componentID, parentNodeID);
    }
}
