package rpg.server;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Set;

public class DistanceRelevantDecider implements RelevantSetDecider {

    private final Node2DQuerySystem querySystem;
    private float maxDistanceSquared;

    public DistanceRelevantDecider(Node2DQuerySystem querySystem, float maxDistanceSquared) {
        this.querySystem = querySystem;
        this.maxDistanceSquared = maxDistanceSquared;
    }

    @Override
    public Set<Node> getRelevantSetForNode(Scene s, Node target) {
        Vector3 worldPos = target.getTransform().getWorldPosition();
        Set<Node> set = querySystem.queryNodesInArea(new Rectangle(worldPos.x - maxDistanceSquared, worldPos.y - maxDistanceSquared, maxDistanceSquared * 2, maxDistanceSquared * 2));
        //Set<Node> set = querySystem.queryAllNodes();
        set.add(target);
        return set;
    }
}
