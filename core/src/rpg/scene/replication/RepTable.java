package rpg.scene.replication;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class RepTable {
    private Class<?> type = null;

    private List<Field> fieldsToSerialize = new ArrayList<>();

    private static Map<Class<?>, RepTable> tables = new HashMap<>();

    /**
     * Get a replication table for a type.
     *
     * @param type the type to find a rep table for.
     * @return the existing rep table, or a new one if it doesn't exist.
     */
    public static RepTable getTableForType(Class<?> type) {
        RepTable t = tables.get(type);
        if (t == null) {
            t = new RepTable(type);
            tables.put(type, t);
        }
        return t;
    }

    /**
     * Discard all replication table mappings, so that new ones may be generated.
     */
    public static void discardAllRepTables() {
        tables.clear();
    }

    private RepTable(Class<?> type) {
        Objects.requireNonNull(type);

        if (type == Object.class) {
            throw new IllegalArgumentException("Can't make a reptable of java.lang.Object.");
        }

        this.type = type;

        // Get full type hierarchy.
        Class<?> c = type;
        Deque<Class<?>> classes = new ArrayDeque<>();
        while (c != Object.class) {
            classes.push(c);
            c = c.getSuperclass();
        }

        // Add fields marked with annotation to field rep list.
        while (!classes.isEmpty()) {
            Class<?> cc = classes.pop();
            List<Field> fields = Arrays.stream(cc.getDeclaredFields()).filter(f -> f.getAnnotation(Replicated.class) != null).collect(Collectors.toList());
            fieldsToSerialize.addAll(fields);
        }

    }

    /**
     * Create a full field replication data set.
     *
     * @param o the object to be replicated
     * @return a dense field replication. You should diff this result with a newer replication to get a delta set.
     */
    public FieldReplicationData replicateFull(Object o) {
        Objects.requireNonNull(o);
        if (o.getClass() != type) {
            throw new IllegalArgumentException("Wrong type, got " + o.getClass().getName() + ", expected " + type.getName());
        }

        FieldReplicationData frd = new FieldReplicationData();
        frd.fieldChangeset = new BitSet(fieldsToSerialize.size());
        frd.fieldChangeset.set(0, frd.fieldChangeset.length(), true);
        frd.fieldData = fieldsToSerialize.stream().map(f -> {
            try {
                return f.get(o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());

        if (frd.fieldData.stream().anyMatch(obj -> obj == null)) {
            throw new RuntimeException("at some point, IllegalAccessException was thrown.");
        }
        return frd;
    }

    /**
     * Apply replication data changes to a destination object.
     *
     * @param data        the data set to pull from
     * @param destination the destination object, must be of the correct type
     */
    public void applyReplicationData(FieldReplicationData data, Object destination) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(destination);
        if (destination.getClass() != type) {
            throw new IllegalArgumentException("destination is not of type " + type.getName() + ", instead it is " + destination.getClass());
        }

        for (int i = 0; i < fieldsToSerialize.size(); i++) {
            Field f = fieldsToSerialize.get(i);
            if (data.fieldChangeset.get(i)) {
                try {
                    f.set(destination, data.fieldData.get(0));
                    data.fieldData.remove(0);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Class<?> getType() {
        return type;
    }
}
