package rpg.scene;

import com.badlogic.gdx.math.Vector2;
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

    public static Node createGravestone(Node parent, boolean local) {
        Node n;
        if (local) n = Node.createLocalNode();
        else n = new Node();

        parent.addChild(n);

        SpriteRenderer spriteRenderer;
        if (local) spriteRenderer = Component.createLocalComponent(SpriteRenderer.class);
        else spriteRenderer = new SpriteRenderer();

        spriteRenderer.setTexture("sprites/gravestone.png");
        spriteRenderer.setOffset(new Vector2(0, 0.5f));

        Node shadow = makeShadowNode(n, local);
        shadow.findComponent(SpriteRenderer.class).setDimensions(new Vector2(1.8f, 1.8f));
        shadow.getTransform().translate(0, 0, 0.005f);

        n.addComponent(spriteRenderer);

        return n;
    }
}
