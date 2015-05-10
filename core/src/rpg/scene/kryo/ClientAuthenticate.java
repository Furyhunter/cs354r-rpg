package rpg.scene.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import rpg.scene.Constants;

public class ClientAuthenticate implements KryoSerializable {
    public int gameVersion = Constants.GAME_VERSION;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(gameVersion, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        gameVersion = input.readInt(true);
    }
}
