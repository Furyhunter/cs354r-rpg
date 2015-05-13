package rpg.scene.components;

import com.badlogic.gdx.math.Vector3;
import rpg.scene.replication.Context;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.NetworkingSceneSystem;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Optional;

/**
 * Created by Corin Hill on 5/13/15.
 */
public class PickupComponent extends Component implements Steppable {
    @Replicated
    protected int item = EXP;

    public static final int EXP = 0;
    public static final int BOMB = 1;
    public static final int AREA_HEAL = 2;

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Node2DQuerySystem n2qs = getParent().getScene().findSystem(Node2DQuerySystem.class);
        Spatial2D s2d = getParent().findComponent(Spatial2D.class);
        Vector3 wp = getParent().getTransform().getWorldPosition();
        if (nss.getContext() == Context.Server) {
            Optional<SimplePlayerComponent> player = n2qs.queryNodesInArea(s2d.getRectangle().setCenter(wp.x,wp.y))
                    .stream().map(n -> n.findComponent(SimplePlayerComponent.class))
                    .filter(p -> p != null).findFirst();
            if (player.isPresent()) {
                if (item == EXP || !player.get().isHoldingItem()) {
                    player.get().pickup(item);
                    getParent().removeFromParent();
                }
            }
        }
    }

    public void setItem(int item) {this.item = item;}
}
