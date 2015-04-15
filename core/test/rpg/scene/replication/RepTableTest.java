package rpg.scene.replication;

import com.badlogic.gdx.math.Vector2;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RepTableTest {

    class SimpleReppable {
        @Replicated
        public Vector2 vec;

        @Replicated
        public Vector2 anotherVec;
    }

    @Test
    public void testSimpleReplication() {
        RepTable.discardAllRepTables();
        RepTable t = RepTable.getTableForType(SimpleReppable.class);

        assertNotNull(t);
        SimpleReppable simpleReppable = new SimpleReppable();
        simpleReppable.vec = new Vector2(123, 456);
        simpleReppable.anotherVec = new Vector2(543, 212);

        FieldReplicationData frd = t.replicateFull(simpleReppable);

        assertNotNull(frd);

        assertEquals("The changeset BitSet should hold two values.", 2, frd.fieldChangeset.size());

        assertEquals("A full replication should have as many data objects as the size of the BitSet",
                2, frd.fieldData.size());

        assertEquals(simpleReppable.vec, frd.fieldData.get(0));
        assertEquals(simpleReppable.anotherVec, frd.fieldData.get(1));
    }

    @Test
    public void testDeltaReplication() {
        RepTable.discardAllRepTables();
        RepTable t = RepTable.getTableForType(SimpleReppable.class);

        assertNotNull(t);
        SimpleReppable simpleReppable = new SimpleReppable();
        simpleReppable.vec = new Vector2(123, 456);
        simpleReppable.anotherVec = new Vector2(543, 212);

        FieldReplicationData frdOld = t.replicateFull(simpleReppable);

        // Some small changes...
        simpleReppable.vec = new Vector2(321, 812);

        FieldReplicationData frdNew = t.replicateFull(simpleReppable);

        // Diff them
        FieldReplicationData frdDiff = frdOld.diff(frdNew);

        assertEquals("Only one property should have changed", 1, frdDiff.fieldChangeset.cardinality());
        assertEquals(new Vector2(321, 812), frdDiff.fieldData.get(0));
    }

    @Test
    public void testDeltaReplicationApplication() {
        RepTable.discardAllRepTables();
        RepTable t = RepTable.getTableForType(SimpleReppable.class);

        assertNotNull(t);
        SimpleReppable simpleReppable = new SimpleReppable();
        simpleReppable.vec = new Vector2(123, 456);
        simpleReppable.anotherVec = new Vector2(543, 212);

        FieldReplicationData frdOld = t.replicateFull(simpleReppable);

        // Some small changes...
        simpleReppable.vec = new Vector2(321, 812);

        FieldReplicationData frdNew = t.replicateFull(simpleReppable);

        // Diff them
        FieldReplicationData frdDiff = frdOld.diff(frdNew);

        t.applyReplicationData(frdDiff, simpleReppable);

        assertEquals(new Vector2(321, 812), simpleReppable.vec);
        assertEquals(new Vector2(543, 212), simpleReppable.anotherVec);
    }
}