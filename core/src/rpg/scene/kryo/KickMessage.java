package rpg.scene.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KickMessage implements KryoSerializable {

    public String message;

    public KickMessage() {

    }

    public KickMessage(String msg) {
        message = msg;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(message);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        message = input.readString();
    }
}
