package rpg.server;

import rpg.scene.Node;
import rpg.scene.Scene;

import java.util.HashSet;
import java.util.Set;

public class AlwaysRelevantDecider implements RelevantSetDecider {

    @Override
    public Set<Node> getRelevantSetForNode(Scene s, Node target) {
        Set<Node> ret = new HashSet<>(1024);
        recurseNode(ret, s.getRoot());

        return ret;
    }

    private void recurseNode(Set<Node> list, Node n) {
        list.add(n);
        n.getChildren().forEach(nn -> recurseNode(list, nn));
    }
}
