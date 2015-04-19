package rpg.scene.kryo;

import rpg.scene.replication.RepTable;

public class ComponentAttach {
    public int componentID;
    public int parentNodeID;
    public int repClassID;

    @Override
    public String toString() {
        Class<?> repType = RepTable.getClassForClassID(repClassID);
        return String.format("ComponentAttach { %d, %d, %s }", componentID, parentNodeID,
                repType == null ? Integer.toString(repClassID) : repType.getName());
    }
}
