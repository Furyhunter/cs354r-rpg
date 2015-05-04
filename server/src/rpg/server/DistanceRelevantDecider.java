package rpg.server;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Set;

public class DistanceRelevantDecider implements RelevantSetDecider {

    private final Node2DQuerySystem querySystem;
    private float maxDistance;

    public DistanceRelevantDecider(Node2DQuerySystem querySystem, float maxDistance) {
        this.querySystem = querySystem;
        this.maxDistance = maxDistance;
    }

    @Override
    public Set<Node> getRelevantSetForNode(Scene s, Node target) {
        Vector3 worldPos = target.getTransform().getWorldPosition();
        Rectangle rect = new Rectangle(worldPos.x - maxDistance, worldPos.y - maxDistance, maxDistance * 2, maxDistance * 2);
        Set<Node> set = querySystem.queryNodesInArea(rect);
        set.add(target);
        return set;
    }
}
