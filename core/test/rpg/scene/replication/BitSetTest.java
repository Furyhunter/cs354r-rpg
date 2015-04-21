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
    public void testSetRange() throws Exception {
        BitSet bitSet = new BitSet(16);

        bitSet.set(0, 4, true);
        bitSet.set(6, 14, true);

        IntStream.range(0, 4).forEach(i -> assertTrue(bitSet.get(i)));
        IntStream.range(4, 6).forEach(i -> assertFalse(bitSet.get(i)));
        IntStream.range(6, 14).forEach(i -> assertTrue(bitSet.get(i)));
        IntStream.range(14, 16).forEach(i -> assertFalse(bitSet.get(i)));

        bitSet.set(0, 4, false);
        bitSet.set(6, 14, false);
        IntStream.range(0, 4).forEach(i -> assertFalse(bitSet.get(i)));
        IntStream.range(6, 14).forEach(i -> assertFalse(bitSet.get(i)));

    }

    @Test
    public void testToggle() throws Exception {
        BitSet bitSet = new BitSet(8);

        // It should start out with all values set to 'false'.

        bitSet.toggle(0);
        bitSet.toggle(1);

        assertTrue(bitSet.get(0));
        assertTrue(bitSet.get(1));

        bitSet.toggle(0);
        bitSet.toggle(1);

        assertFalse(bitSet.get(0));
        assertFalse(bitSet.get(1));
    }

    @Test
    public void testToggleRange() throws Exception {
        BitSet bitSet = new BitSet(8);

        bitSet.toggle(0, 4);

        IntStream.range(0, 4).forEach(i -> assertTrue(bitSet.get(i)));

        bitSet.toggle(0, 4);

        IntStream.range(0, 4).forEach(i -> assertFalse(bitSet.get(i)));
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