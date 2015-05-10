package rpg.scene.kryo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import rpg.game.*;
import rpg.scene.containers.*;
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

                // NEVER MOVE THESE TWO
                ClientAuthenticate.class,
                KickMessage.class,

                Vector2.class,
                Vector3.class,
                Quaternion.class,
                Matrix3.class,
                Matrix4.class,
                Color.class,

                Bullet.class,
                SimpleBullet.class,

                SimpleEnemy.class,

                BitSet.class,
                RPCInvocation.class,

                AssetContainer.class,
                BitmapFontContainer.class,
                MusicContainer.class,
                SoundContainer.class,
                TextureAtlasContainer.class,
                TextureContainer.class,

                FieldReplicationData.class,

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
                RPCMessage.class,

                NodeReferenceContainer.class,
                ComponentReferenceContainer.class,
        };

        Arrays.stream(classes).forEach(k::register);

        k.register(byte[].class, new DeflateSerializer(new DefaultArraySerializers.ByteArraySerializer()));
    }
}
