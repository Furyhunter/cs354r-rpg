package rpg.scene.replication;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class BitSetTest {

    @Test
    public void testSet() throws Exception {
        BitSet bitSet = new BitSet(16);

        bitSet.set(0, true);

        assertTrue("bit 0 must be true", bitSet.get(0));
        IntStream.range(1, 8).forEach(i -> assertFalse("bit " + i + " must be false", bitSet.get(i)));
    }

    @Test
    public void testSetMultiple() throws Exception {
        BitSet bitSet = new BitSet(16);

        bitSet.set(0, 4, true);
        bitSet.set(6, 14, true);

        IntStream.range(0, 4).forEach(i -> assertTrue(bitSet.get(i)));
        IntStream.range(4, 6).forEach(i -> assertFalse(bitSet.get(i)));
        IntStream.range(6, 14).forEach(i -> assertTrue(bitSet.get(i)));
        IntStream.range(14, 16).forEach(i -> assertFalse(bitSet.get(i)));
    }

    @Test
    public void testKryoSerialize() throws Exception {
        BitSet bitSet = new BitSet(32); // 4 bytes

        bitSet.set(0, 4, true);
        bitSet.set(6, 14, true);

        Kryo k = new Kryo();
        k.register(BitSet.class);

        byte[] buf = new byte[64];
        Output output = new Output();
        output.setBuffer(buf);
        k.writeObject(output, bitSet);

        Input i = new Input();
        i.setBuffer(buf);

        BitSet serial = k.readObject(i, BitSet.class);

        assertEquals(serial, bitSet);
    }
}