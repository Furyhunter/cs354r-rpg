package rpg.scene.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ComponentReferenceContainer implements KryoSerializable {
    public int componentID;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(componentID);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        componentID = input.readInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ComponentReferenceContainer)) {
            return false;
        }
        ComponentReferenceContainer r = (ComponentReferenceContainer) obj;
        if (r.componentID != this.componentID) {
            return false;
        }
        return true;
    }
}
