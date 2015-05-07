package rpg.scene.components;

import rpg.game.SimplePlayerBullet;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Corin Hill on 5/6/15.
 */
public class SimplePlayerBulletComponent extends SimpleBulletComponent {

    public SimplePlayerBulletComponent() {
        bullet = new SimplePlayerBullet();
    }

    @Override
    protected void checkCollisions() {
        Node2DQuerySystem n2qs = getParent().getScene().findSystem(Node2DQuerySystem.class);
        Objects.requireNonNull(n2qs);

        Spatial2D s = getParent().<Spatial2D>findComponent(Spatial2D.class);
        if (s != null) {
            Set<SimpleEnemyComponent> nodes = n2qs.queryNodesInArea(s.getRectangle()).stream().map(
                    n -> n.<SimpleEnemyComponent>findComponent(SimpleEnemyComponent.class)
            ).filter(e -> e != null).collect(Collectors.toSet());
            if (nodes.size() > 0) {
                nodes.forEach(e -> e.hurt(bullet.getDamage()));
                bullet.age(bullet.getLIFETIME());
            }
        }
    }
}
