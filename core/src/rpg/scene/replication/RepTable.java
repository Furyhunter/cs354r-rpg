package rpg.scene.replication;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.Component;
import rpg.scene.kryo.ComponentReferenceContainer;
import rpg.scene.kryo.NodeReferenceContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class RepTable {
    private static Map<Class<?>, RepTable> tables = new HashMap<>();
    private static BiMap<Class<?>, Integer> classIDs = HashBiMap.create();

    private Class<?> type = null;

    private List<Integer> fieldsToSerializeFieldAccess = new ArrayList<>();
    private FieldAccess fieldAccess;

    private BiMap<String, Integer> methodNameToAccessorID = HashBiMap.create();
    private BiMap<Integer, Integer> methodAccessorIDToRepID = HashBiMap.create();
    private Map<Integer, RPC> methodRepIDToRPCAnnotation = new TreeMap<>();
    private MethodAccess methodAccess;

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

        fieldAccess = FieldAccess.get(type);
        methodAccess = MethodAccess.get(type);

        // Add fields marked with annotation to field rep list.
        while (!classes.isEmpty()) {
            Class<?> cc = classes.pop();
            List<Field> fields = Arrays.stream(cc.getDeclaredFields()).filter(f -> f.getAnnotation(Replicated.class) != null).collect(Collectors.toList());
            if (fields.stream().anyMatch(f -> Modifier.isPrivate(f.getModifiers()))) {
                throw new RuntimeException("Replicated fields may not be private.");
            }

            fields.forEach(f -> fieldsToSerializeFieldAccess.add(fieldAccess.getIndex(f.getName())));

            List<Method> methods = Arrays.stream(cc.getDeclaredMethods())
                    .filter(m -> m.getAnnotation(RPC.class) != null)
                    .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                    .collect(Collectors.toList());

            if (methods.stream().anyMatch(m -> Modifier.isPrivate(m.getModifiers()))) {
                throw new RuntimeException("RPC methods may not be private.");
            }
            methods.forEach(m -> {
                int newMethodID = methodCounter++;
                methodNameToAccessorID.put(m.getName(), methodAccess.getIndex(m.getName()));
                methodAccessorIDToRepID.put(methodAccess.getIndex(m.getName()), newMethodID);
                methodRepIDToRPCAnnotation.put(newMethodID, m.getAnnotation(RPC.class));
            });
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
        frd.fieldChangeset = new BitSet(fieldsToSerializeFieldAccess.size());
        frd.fieldChangeset.set(0, fieldsToSerializeFieldAccess.size(), true);
        frd.fieldData = fieldsToSerializeFieldAccess.stream().map(f -> fieldAccess.get(o, f)).map(RepTable::mapToReplicateContainer).collect(Collectors.toList());

        return frd;
    }

    public static Scene scene;

    private static Object mapToReplicateContainer(Object o) {
        if (scene == null) return o;

        if (o instanceof Node) {
            NodeReferenceContainer nodeReferenceContainer = new NodeReferenceContainer();
            nodeReferenceContainer.nodeID = ((Node) o).getNetworkID();
            return nodeReferenceContainer;
        }
        if (o instanceof Component) {
            ComponentReferenceContainer componentReferenceContainer = new ComponentReferenceContainer();
            componentReferenceContainer.componentID = ((Component) o).getNetworkID();
            return componentReferenceContainer;
        }
        return o;
    }

    private static Object mapToActualObject(Object o) {
        if (scene == null) return o;

        if (o instanceof NodeReferenceContainer) {
            return scene.findNode(((NodeReferenceContainer) o).nodeID);
        }
        if (o instanceof ComponentReferenceContainer) {
            return scene.findComponent(((ComponentReferenceContainer) o).componentID);
        }
        return o;
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

        if (destination instanceof Component) {
            Component component = (Component) destination;
            component.onPreApplyReplicateFields();
        }

        List<Object> fieldData = new LinkedList<>(data.fieldData);
        for (int i = 0; i < fieldsToSerializeFieldAccess.size(); i++) {
            int f = fieldsToSerializeFieldAccess.get(i);
            if (data.fieldChangeset.get(i)) {
                fieldAccess.set(destination, f, mapToActualObject(fieldData.get(0)));
                fieldData.remove(0);
            }
        }

        if (destination instanceof Component) {
            Component component = (Component) destination;
            component.onPostApplyReplicatedFields();
        }
    }

    public Class<?> getType() {
        return type;
    }

    public int getRPCMethodID(String methodName) {
        return methodAccessorIDToRepID.get(methodNameToAccessorID.get(methodName));
    }

    public RPCInvocation getRPCInvocation(String methodName, Object... arguments) {
        int id = getRPCMethodID(methodName);
        RPCInvocation r = new RPCInvocation();
        r.methodId = id;
        r.arguments = Arrays.asList(arguments);
        return r;
    }

    public RPC.Target getRPCTarget(String methodName) {
        return methodRepIDToRPCAnnotation.get(methodAccessorIDToRepID.get(methodNameToAccessorID.get(methodName))).target();
    }

    public RPC.Target getRPCTarget(int repID) {
        return methodRepIDToRPCAnnotation.get(repID).target();
    }

    public void invokeMethod(Object target, int repID, Object... args) {
        int accessorID = methodAccessorIDToRepID.inverse().get(repID);
        methodAccess.invoke(target, accessorID, args);
    }

    public void invokeMethod(Object target, String name, Object... args) {
        int accessorID = methodNameToAccessorID.get(name);
        methodAccess.invoke(target, accessorID, args);
    }

    public void invokeMethod(Object target, RPCInvocation invocation) {
        invokeMethod(target, invocation.methodId, invocation.arguments.toArray());
    }
}
