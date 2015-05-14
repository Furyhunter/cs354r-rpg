package rpg.scene.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.NodeFactory;
import rpg.scene.systems.NetworkingSceneSystem;
import rpg.scene.systems.Node2DQuerySystem;

import java.util.Optional;

/**
 * Created by Corin Hill on 5/13/15.
 */
public class SimpleBossComponent extends Component implements Steppable, Killable {
    private static float VISION_RADIUS = 20;
    //private static float ATTACK_RADIUS = 15;
    private static float DROP_RATE = 0.5f;

    private Node target = null;

    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);

    }

    private void checkTarget() {
        if (target == null || target.getParent() == null) {
            Node2DQuerySystem nqs = getParent().getScene().findSystem(Node2DQuerySystem.class);
            Vector3 wp = getParent().getTransform().getWorldPosition();
            Rectangle r = new Rectangle(wp.x-VISION_RADIUS,wp.y-VISION_RADIUS,
                    VISION_RADIUS*2,VISION_RADIUS*2);
            if (nqs != null) {
                Optional<SimplePlayerComponent> p = nqs.queryNodesInArea(r).stream()
                        .map(n -> n.findComponent(SimplePlayerComponent.class))
                        .filter(n -> n != null).findAny();
                if (p.isPresent()) target = p.get().getParent();
                else target = null;
            }
        }
    }

    private void generateEXP() {
        Transform t = getParent().getTransform();
        for (int i = 0; i < MathUtils.random(5,10); ++i) {
            Node n = NodeFactory.createEXPDrop(getParent().findRoot(), false);
            n.getTransform().setPosition(new Vector3(t.getWorldPosition().x,t.getWorldPosition().y,0.1f));

            PickupComponent p = new PickupComponent();
            p.setItem(PickupComponent.EXP);
            ArcToGroundComponent a = new ArcToGroundComponent();

            n.addComponent(p);
            n.addComponent(a);
        }

    }

    private void generateDrop() {
        Transform t = getParent().getTransform();
        for (int i = 0; i < MathUtils.random(0,4); ++i) {
            int item;
            if (MathUtils.random() < 0.25) {
                item = PickupComponent.HEAL;
            } else {
                item = PickupComponent.BOMB;
            }
            Node n = NodeFactory.createDrop(getParent().findRoot(), false, item);
            n.getTransform().setPosition(new Vector3(t.getWorldPosition().x,t.getWorldPosition().y,0.005f));

            PickupComponent p = new PickupComponent();
            p.setItem(item);
            ArcToGroundComponent a = new ArcToGroundComponent();

            n.addComponent(p);
            n.addComponent(a);
        }
    }

    public void kill() {
        generateEXP();
        if (MathUtils.random() < DROP_RATE) {
            generateDrop();
        }
        getParent().removeFromParent();
    }
}
