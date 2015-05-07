package rpg.scene.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class NodeReferenceContainer implements KryoSerializable {
    public int nodeID;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(nodeID);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        nodeID = input.readInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof NodeReferenceContainer)) {
            return false;
        }
        NodeReferenceContainer c = ((NodeReferenceContainer) obj);

        if (c.nodeID != nodeID) {
            return false;
        }
        return true;
    }
}
