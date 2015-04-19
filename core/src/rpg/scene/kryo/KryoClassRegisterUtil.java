package rpg.scene.kryo;

import com.badlogic.gdx.math.*;
import com.esotericsoftware.kryo.Kryo;
import rpg.scene.replication.BitSet;
import rpg.scene.replication.FieldReplicationData;
import rpg.scene.replication.RPCInvocation;
import rpg.scene.replication.RPCMessage;

import java.util.Arrays;

public final class KryoClassRegisterUtil {

    private KryoClassRegisterUtil() {

    }

    public static void registerAll(Kryo k) {
        Class<?>[] classes = new Class<?>[]{

                Vector2.class,
                Vector3.class,
                Quaternion.class,
                Matrix3.class,
                Matrix4.class,

                BitSet.class,
                RPCInvocation.class,

                // Messages
                BeginTick.class,
                EndTick.class,

                NodeAttach.class,
                NodeDetach.class,
                NodeReattach.class,

                ComponentAttach.class,
                ComponentDetach.class,
                ComponentReattach.class,

                FieldReplicateMessage.class,

                FieldReplicationData.class,
                RPCMessage.class,
        };

        Arrays.stream(classes).forEach(k::register);
    }
}
