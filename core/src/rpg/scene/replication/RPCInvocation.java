package rpg.scene.replication;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class RPCInvocation implements KryoSerializable {

    public int methodId = 0;
    public List<Object> arguments = new ArrayList<>();

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(methodId, true);
        output.writeInt(arguments.size(), true);
        arguments.forEach(a -> kryo.writeClassAndObject(output, a));
    }

    @Override
    public void read(Kryo kryo, Input input) {
        methodId = input.readInt(true);
        int size = input.readInt(true);
        arguments = new ArrayList<>();
        IntStream.range(0, size).forEach(i -> arguments.add(kryo.readClassAndObject(input)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RPCInvocation)) {
            return false;
        }
        RPCInvocation rpc = (RPCInvocation) obj;
        if (rpc.methodId != methodId) {
            return false;
        }
        return rpc.arguments.equals(arguments);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Invoke ");
        b.append(methodId);
        b.append(": ");
        b.append(arguments);
        return b.toString();
    }
}
