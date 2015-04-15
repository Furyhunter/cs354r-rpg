package rpg.scene.replication;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class FieldReplicationData implements KryoSerializable {

    public BitSet fieldChangeset;
    public List<Object> fieldData;

    @Override
    public void write(Kryo kryo, Output output) {
        if (fieldChangeset.cardinality() != fieldData.size()) {
            throw new RuntimeException("fieldChangeset != fieldData in FieldReplicationData");
        }
        kryo.writeObject(output, fieldChangeset);
        fieldData.forEach(o -> kryo.writeClassAndObject(output, o));
    }

    @Override
    public void read(Kryo kryo, Input input) {
        fieldChangeset = kryo.readObject(input, BitSet.class);
        fieldData = new ArrayList<>();
        for (int i = 0; i < fieldChangeset.length(); i++) {
            fieldData.add(kryo.readClassAndObject(input));
        }
    }

    public FieldReplicationData diff(FieldReplicationData recent) {
        FieldReplicationData frd = new FieldReplicationData();
        if (recent.fieldChangeset.length() != fieldChangeset.length()) {
            throw new IllegalArgumentException("These rep datas are not the same size and we can't compare them.");
        }
        if (recent.fieldChangeset.cardinality() != recent.fieldData.size()) {
            throw new RuntimeException("Recent bitset cardinality != data list size");
        }
        if (fieldChangeset.cardinality() != fieldData.size()) {
            throw new RuntimeException("This (old) bitset cardinality != data list size");
        }
        frd.fieldChangeset = new BitSet(fieldChangeset.length());

        for (int i = 0; i < fieldChangeset.length(); i++) {
            Object ours = fieldData.get(i);
            Object theirs = fieldData.get(i);

            if (!ours.equals(theirs)) {
                frd.fieldChangeset.set(i, true);
                frd.fieldData.add(theirs);
            }
        }

        return frd;
    }
}
