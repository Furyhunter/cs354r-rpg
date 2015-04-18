package rpg.server;

import rpg.scene.Node;
import rpg.scene.Scene;

import java.util.Set;

public interface RelevantSetDecider {
    /**
     * Get the relevant set around a target node, usually a Player's possessed node.
     * <p>
     * For a node to be relevant, usually there is some heuristic such as "only nearby
     * objects" or "objects that are in view from this node". Where it gets the
     * information to specify what is and isn't relevant is left to the implementation
     * to decide.
     *
     * @param s      the scene to get the relevant set from
     * @param target the target "source" node
     * @return a list of relevant nodes for the target node.
     */
    Set<Node> getRelevantSetForNode(Scene s, Node target);
}
