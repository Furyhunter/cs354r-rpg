package rpg.scene.replication;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class RepTable {
    private Class<?> type = null;

    private List<Field> fieldsToSerialize = new ArrayList<>();
    private BiMap<Method, Integer> rpcMethods = HashBiMap.create();

    private static Map<Class<?>, RepTable> tables = new HashMap<>();

    private static BiMap<Class<?>, Integer> classIDs = HashBiMap.create();
    private static int classIDsIncrement = 0;

    private int methodCounter = 0;

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
            classIDs.put(type, type.getName().hashCode());
        }
        return t;
    }

    public static int getClassIDForType(Class<?> type) {
        if (!classIDs.containsKey(type)) {
            classIDs.put(type, type.getName().hashCode());
        }
        return classIDs.get(type);
    }

    public static Class<?> getClassForClassID(int id) {
        return classIDs.inverse().get(id);
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
            if (fields.stream().anyMatch(f -> Modifier.isPrivate(f.getModifiers()))) {
                throw new RuntimeException("Replicated fields may not be private.");
            }
            fieldsToSerialize.addAll(fields);

            List<Method> methods = Arrays.stream(cc.getDeclaredMethods()).filter(m -> m.getAnnotation(RPC.class) != null).collect(Collectors.toList());
            if (methods.stream().anyMatch(m -> Modifier.isPrivate(m.getModifiers()))) {
                throw new RuntimeException("RPC methods may not be private.");
            }
            methods.forEach(m -> rpcMethods.put(m, methodCounter++));
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

        List<Boolean> illegalAccessExceptions = new ArrayList<>();
        FieldReplicationData frd = new FieldReplicationData();
        frd.fieldChangeset = new BitSet(fieldsToSerialize.size());
        frd.fieldChangeset.set(0, fieldsToSerialize.size(), true);
        frd.fieldData = fieldsToSerialize.stream().map(f -> {
            f.setAccessible(true);
            try {
                return f.get(o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                illegalAccessExceptions.add(true);
                return null;
            }
        }).collect(Collectors.toList());

        if (illegalAccessExceptions.size() > 0) {
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
            f.setAccessible(true);
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

    public Method getRPCMethod(int repID) {
        return rpcMethods.inverse().get(repID);
    }

    public Method getRPCMethod(String methodName) {
        Optional<Method> s = rpcMethods.keySet().stream().filter(m -> m.getName().equals(methodName)).findFirst();
        if (s.get() != null) {
            return s.get();
        } else {
            throw new RuntimeException("no registered RPC with that name exists");
        }
    }

    public int getRPCMethodID(Method m) {
        return rpcMethods.get(m);
    }

    public int getRPCMethodID(String methodName) {
        Optional<Method> s = rpcMethods.keySet().stream().filter(m -> m.getName().equals(methodName)).findFirst();
        if (s.get() != null) {
            return getRPCMethodID(s.get());
        } else {
            throw new RuntimeException("no registered RPC with that name exists");
        }
    }

    public RPCInvocation getRPCInvocation(String methodName, Object... arguments) {
        int id = getRPCMethodID(methodName);
        RPCInvocation r = new RPCInvocation();
        r.methodId = id;
        r.arguments = Arrays.asList(arguments);
        return r;
    }

    public RPC.Target getRPCTarget(String methodName) {
        return getRPCMethod(methodName).getAnnotation(RPC.class).target();
    }
}
