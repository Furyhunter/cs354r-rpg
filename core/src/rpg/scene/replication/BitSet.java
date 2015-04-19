package rpg.scene.replication;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.stream.IntStream;

/**
 * A space-optimized BitSet serializable by Kryo.
 *
 * @author Ronald Kinard
 */
public class BitSet implements KryoSerializable {

    private static final int BITMASK = 0xff;
    private byte[] sections;
    private int numSections;

    /**
     * Creates a bitset of 8 bits in size.
     */
    public BitSet() {
        numSections = 1;
        sections = new byte[1];
    }

    /**
     * Creates a BitSet that fits the given size.
     * Internally, the number of bits available in the bitset will be as many bytes needed to fit the given size.
     * In worst case scenario, there may be 7 extra unused bits. This is considerably better than the java util
     * version.
     *
     * @param fitSize
     */
    public BitSet(int fitSize) {
        numSections = fitSize / 8; // integer divide by num bits in byte
        if (fitSize % 8 != 0) {
            numSections++;
        }
        sections = new byte[numSections];
    }

    /**
     * @param bit   index of bit to set
     * @param value 1 for true, 0 for false
     */
    public void set(int bit, boolean value) {
        if (bit < 0) {
            throw new IndexOutOfBoundsException("bit < 0");
        }
        if (bit >= numSections * 8) {
            throw new IndexOutOfBoundsException("bit > numSections * 8");
        }
        int actualBit = bit % 8;
        int section = bit / 8;
        int val = value ? 1 : 0;

        sections[section] |= (val << actualBit);
    }

    /**
     * Sets some bits in a range.
     *
     * @param from  inclusive first index
     * @param to    exclusive last index
     * @param value value to set
     */
    public void set(int from, int to, boolean value) {
        IntStream.range(from, to).forEach(i -> set(i, value));
    }

    /**
     * @param bit the index
     * @return the value of the bit index
     */
    public boolean get(int bit) {
        int actualBit = bit % 8;
        int section = bit / 8;

        int bitVal = sections[section] & (1 << actualBit);
        return bitVal > 0;
    }

    /**
     * @return the size of this bitset in bytes (8 bits)
     */
    public int getNumSections() {
        return numSections;
    }

    /**
     * @return the size of this bitset in bits
     */
    public int getSize() {
        return numSections * 8;
    }

    /**
     * @return the number of set bits
     */
    public int cardinality() {
        int num = 0;
        for (int i = 0; i < numSections * 8; i++) {
            if (get(i)) {
                num++;
            }
        }

        return num;
    }

    public void toggle(int bit) {
        if (bit < 0) {
            throw new IndexOutOfBoundsException("bit < 0");
        }
        if (bit >= numSections * 8) {
            throw new IndexOutOfBoundsException("bit > numSections * 8");
        }
        int actualBit = bit % 8;
        int section = bit / 8;

        sections[section] ^= (1 << actualBit);
    }

    public void toggle(int from, int to) {
        IntStream.range(from, to).forEach(i -> toggle(i));
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(numSections);
        output.writeBytes(sections);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        numSections = input.readByte();
        sections = input.readBytes(numSections);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BitSet)) {
            return false;
        }
        BitSet o = (BitSet) obj;
        if (o.getNumSections() != getNumSections()) {
            // differing sizes
            return false;
        }
        for (int i = 0; i < getNumSections(); i++) {
            if (sections[i] != o.sections[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int size = getSize();
        for (int i = size - 1; i >= 0; i--) {
            b.append(get(i) ? "1" : "0");
        }
        return b.toString();
    }
}
