package rpg.scene.replication;

import org.reflections.Reflections;
import rpg.game.Bullet;
import rpg.scene.components.Component;

public final class RepTableInitializeUtil {
    private RepTableInitializeUtil() {

    }

    public static void initializeRepTables() {
        Reflections r = new Reflections("rpg");
        r.getSubTypesOf(Component.class).stream().forEach(RepTable::getTableForType);
        r.getSubTypesOf(Bullet.class).stream().forEach(RepTable::getTableForType);
    }
}
