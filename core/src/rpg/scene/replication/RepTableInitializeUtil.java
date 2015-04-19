package rpg.scene.replication;

import rpg.scene.components.Transform;

import java.util.Arrays;

public final class RepTableInitializeUtil {
    private RepTableInitializeUtil() {

    }

    public static void initializeRepTables() {
        Class<?>[] classes = new Class<?>[]{
                Transform.class,
        };

        Arrays.stream(classes).forEach(RepTable::getTableForType);
    }
}
