package rpg.scene.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.NodeFactory;
import rpg.scene.replication.Context;
import rpg.scene.replication.Replicated;
import rpg.scene.systems.NetworkingSceneSystem;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Corin Hill on 5/13/15.
 */
public class ExplosionComponent extends Component implements Steppable {
    @Replicated
    protected float scaleRate = 8;

    public static final float scaleAccel = 10;


    @Replicated
    protected Node creator;
    private UnitComponent creatorUnitComponent;

    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Transform t = getParent().getTransform();

        scaleRate -= scaleAccel * deltaTime;
        t.setScale(t.getScale().cpy().scl(scaleRate * deltaTime));

        checkCollisions();
        if (t.getScale().len() < 0.05 && nss.getContext() == Context.Server) {
            getParent().removeFromParent();
        }
    }
    protected void checkCollisions() {
        Node2DQuerySystem n2qs = getParent().getScene().findSystem(Node2DQuerySystem.class);
        Spatial2D s = getParent().findComponent(Spatial2D.class);

        Vector3 worldPosition = getParent().getTransform().getWorldPosition();

        Set<Node> nodes = n2qs.queryNodesInArea(s.getRectangle().setCenter(worldPosition.x, worldPosition.y));
        nodes.stream()
                .map(n -> n.findComponent(UnitComponent.class))
                .forEach(c -> {
                    if (c == null) return;
                    if (getParent() == null || getParent().getParent() == null) return;
                    if (c.getFaction() != creatorUnitComponent.getFaction()) {
                        c.hurt(this,5);
                    }
                });
    }
    public void setCreator(Node creator) {
        this.creator = creator;
        this.creatorUnitComponent = creator.findComponent(UnitComponent.class);
    }
    public Node getCreator() {return creator;}
}
