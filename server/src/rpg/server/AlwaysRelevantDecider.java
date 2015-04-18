package rpg.server;

import rpg.scene.Node;
import rpg.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public class AlwaysRelevantDecider implements RelevantSetDecider {

    @Override
    public List<Node> getRelevantSetForNode(Scene s, Node target) {
        ArrayList<Node> ret = new ArrayList<>();
        recurseNode(ret, s.getRoot());

        return ret;
    }

    private void recurseNode(List<Node> list, Node n) {
        list.add(n);
        n.getChildren().forEach(nn -> recurseNode(list, nn));
    }
}
