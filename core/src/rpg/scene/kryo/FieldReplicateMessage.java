package rpg.scene.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import rpg.scene.replication.FieldReplicationData;

public class FieldReplicateMessage implements KryoSerializable {
    public int componentID;
    public FieldReplicationData fieldReplicationData;

    public FieldReplicateMessage() {

    }

    public FieldReplicateMessage(int componentID, FieldReplicationData fieldReplicationData) {
        this.componentID = componentID;
        this.fieldReplicationData = fieldReplicationData;
    }

    @Override
    public String toString() {
        return String.format("FieldReplicateMessage %d: %s", componentID, fieldReplicationData.toString());
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(componentID);
        kryo.writeObject(output, fieldReplicationData);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        componentID = input.readInt();
        fieldReplicationData = kryo.readObject(input, FieldReplicationData.class);
    }
}
