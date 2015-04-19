package rpg.scene.replication;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FieldReplicationData implements KryoSerializable {

    public BitSet fieldChangeset = new BitSet();
    public List<Object> fieldData = new ArrayList<>();

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
        for (int i = 0; i < fieldChangeset.cardinality(); i++) {
            fieldData.add(kryo.readClassAndObject(input));
        }
    }

    public FieldReplicationData diff(FieldReplicationData recent) {
        FieldReplicationData frd = new FieldReplicationData();
        if (recent.fieldChangeset.getSize() != fieldChangeset.getSize()) {
            throw new IllegalArgumentException("These rep datas are not the same size and we can't compare them.");
        }
        if (recent.fieldChangeset.cardinality() != recent.fieldData.size()) {
            throw new RuntimeException("Recent bitset cardinality != data list size");
        }
        if (fieldChangeset.cardinality() != fieldData.size()) {
            throw new RuntimeException("This (old) bitset cardinality != data list size");
        }
        frd.fieldChangeset = new BitSet(fieldChangeset.getSize());

        for (int i = 0; i < fieldChangeset.getSize() && i < fieldData.size(); i++) {
            Object ours = fieldData.get(i);
            Object theirs = recent.fieldData.get(i);

            if (!Objects.equals(ours, theirs)) {
                frd.fieldChangeset.set(i, true);
                frd.fieldData.add(theirs);
            }
        }

        return frd;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FieldReplicationData)) {
            return false;
        }
        FieldReplicationData o = (FieldReplicationData) obj;

        if (!o.fieldChangeset.equals(fieldChangeset)) {
            return false;
        }
        if (!o.fieldData.equals(fieldData)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FieldReplicationData: ");
        sb.append(fieldChangeset);
        sb.append(" ");
        sb.append(fieldData.toString());
        return sb.toString();
    }
}
