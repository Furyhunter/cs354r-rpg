package rpg.scene.components;

import com.badlogic.gdx.math.Vector3;
import rpg.game.SimpleEnemyBullet;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Corin Hill on 5/6/15.
 */
public class SimpleEnemyBulletComponent extends SimpleBulletComponent {

    public SimpleEnemyBulletComponent() {
        bullet = new SimpleEnemyBullet();
    }

    @Override
    protected void checkCollisions() {
        Node2DQuerySystem n2qs = getParent().getScene().findSystem(Node2DQuerySystem.class);
        Objects.requireNonNull(n2qs);

        Vector3 wp = getParent().getTransform().getWorldPosition().cpy();
        Spatial2D s = getParent().<Spatial2D>findComponent(Spatial2D.class);
        if (s != null) {
            Set<SimplePlayerComponent> nodes = n2qs.queryNodesInArea(s.getRectangle().setCenter(wp.x,wp.y)).stream()
                    .map(n -> n.<SimplePlayerComponent>findComponent(SimplePlayerComponent.class)
            ).filter(e -> e != null).collect(Collectors.toSet());
            if (nodes.size() > 0) {
                nodes.forEach(e -> e.hurt(bullet.getDamage()));
                bullet.age(bullet.getLIFETIME());
            }
        }
    }
}
