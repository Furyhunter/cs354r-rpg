package rpg.scene.replication;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class RPCMessage implements KryoSerializable {

    public int targetNetworkID = 0;
    public RPCInvocation invocation;

    public RPCMessage() {

    }

    public RPCMessage(int targetNetworkID, RPCInvocation invocation) {
        this.targetNetworkID = targetNetworkID;
        this.invocation = invocation;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(targetNetworkID, true);
        kryo.writeObject(output, invocation);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        targetNetworkID = input.readInt(true);
        invocation = kryo.readObject(input, RPCInvocation.class);
    }
}
