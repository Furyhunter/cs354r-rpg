package rpg.scene;

import rpg.scene.components.Component;
import rpg.scene.components.SpriteRenderer;

public final class NodeFactory {
    private NodeFactory() {

    }

    public static Node makeShadowNode(Node parent, boolean local) {
        Node n;
        if (local) n = Node.createLocalNode();
        else n = new Node();

        parent.addChild(n);

        SpriteRenderer spriteRenderer;
        if (local) {
            spriteRenderer = Component.createLocalComponent(SpriteRenderer.class);
        } else {
            spriteRenderer = new SpriteRenderer();
        }
        spriteRenderer.setBillboard(false);
        spriteRenderer.setTexture("sprites/shadow.png");

        n.getTransform().scale(0.5f);
        n.addComponent(spriteRenderer);
        return n;
    }
}
