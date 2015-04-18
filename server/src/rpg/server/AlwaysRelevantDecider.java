package rpg.server;

import rpg.scene.Node;
import rpg.scene.Scene;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class AlwaysRelevantDecider implements RelevantSetDecider {

    @Override
    public Set<Node> getRelevantSetForNode(Scene s, Node target) {
        Set<Node> ret = new TreeSet<>(Comparator.comparingInt(Node::getNetworkID));
        recurseNode(ret, s.getRoot());

        return ret;
    }

    private void recurseNode(Set<Node> list, Node n) {
        list.add(n);
        n.getChildren().forEach(nn -> recurseNode(list, nn));
    }
}
