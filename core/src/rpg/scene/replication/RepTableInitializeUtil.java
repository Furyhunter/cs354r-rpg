package rpg.scene.replication;

import org.reflections.Reflections;
import rpg.scene.components.Component;

public final class RepTableInitializeUtil {
    private RepTableInitializeUtil() {

    }

    public static void initializeRepTables() {
        new Reflections("rpg").getSubTypesOf(Component.class).stream().forEach(RepTable::getTableForType);
    }
}
