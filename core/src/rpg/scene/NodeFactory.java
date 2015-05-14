package rpg.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.components.*;

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

        DetachAfterDelayComponent detachAfterDelayComponent = Component.createLocalComponent(DetachAfterDelayComponent.class);
        detachAfterDelayComponent.timeRemaining = 60;
        n.addComponent(detachAfterDelayComponent);
        n.addComponent(spriteRenderer);

        return n;
    }

    public static Node makeGrassDoodad(Node parent, boolean local) {
        Node n;
        if (local) n = Node.createLocalNode();
        else n = new Node();

        parent.addChild(n);

        SpriteRenderer spriteRenderer;
        if (local) spriteRenderer = Component.createLocalComponent(SpriteRenderer.class);
        else spriteRenderer = new SpriteRenderer();

        spriteRenderer.setTexture("sprites/grass.png");
        spriteRenderer.setOffset(new Vector2(0, 0.5f));
        n.getTransform().setScale(new Vector3(0.5f, 0.5f, 1));

        n.addComponent(spriteRenderer);

        return n;
    }

    public static Node createEXPDrop(Node parent, boolean local) {
        Node n;
        if (local) n = Node.createLocalNode();
        else n = new Node();

        parent.addChild(n);

        SpriteRenderer sr;
        if (local) sr = Component.createLocalComponent(SpriteRenderer.class);
        else sr = new SpriteRenderer();
        sr.setTexture("sprites/gold-3.png");
        sr.setDimensions(new Vector2(0.4f, 0.4f));

        DetachAfterDelayComponent d = Component.createLocalComponent(DetachAfterDelayComponent.class);
        d.timeRemaining = 20;

        Node shadow = makeShadowNode(n, local);
        shadow.findComponent(SpriteRenderer.class).setDimensions(new Vector2(0.4f, 0.4f));
        shadow.getTransform().translate(0, 0, 0.005f);

        n.addComponent(sr);
        n.addComponent(d);

        return n;
    }

    public static Node createDrop(Node parent, boolean local, int item) {
        Node n;
        if (local) n = Node.createLocalNode();
        else n = new Node();

        parent.addChild(n);

        SpriteRenderer sr;
        if (local) sr = Component.createLocalComponent(SpriteRenderer.class);
        else sr = new SpriteRenderer();
        if (item == PickupComponent.BOMB) {
            sr.setTexture("sprites/orange.png");
        } else if (item == PickupComponent.HEAL) {
            sr.setTexture("sprites/red-flask.png");
        }
        sr.setDimensions(new Vector2(0.4f, 0.4f));

        DetachAfterDelayComponent d = Component.createLocalComponent(DetachAfterDelayComponent.class);
        d.timeRemaining = 20;

        n.addComponent(sr);
        n.addComponent(d);

        return n;
    }
}
