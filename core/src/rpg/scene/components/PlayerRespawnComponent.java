package rpg.scene.components;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPC;
import rpg.scene.systems.InputSystem;
import rpg.scene.systems.NetworkingSceneSystem;

import static rpg.scene.systems.InputSystem.EventType.KeyDown;
import static rpg.scene.systems.InputSystem.EventType.KeyUp;

/**
 * Created by Corin Hill on 5/13/15.
 */
public class PlayerRespawnComponent extends Component implements Steppable, InputEventListener {

    private boolean keyR = false;

    @Override
    public void processInputEvent(InputSystem.InputEvent event) {
        if (event.getType() == KeyDown) {
            if( event.getButton() == Input.Keys.R) {
                    keyR = true;
            }
        }
        if (event.getType() == KeyUp) {
            if( event.getButton() == Input.Keys.R) {
                keyR = false;
            }
        }
    }

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        if (nss.getContext() == Context.Client && getParent().isPossessed()) {
            if (keyR) {
                sendRPC("respawn");
            }
        }
    }

    @RPC(target = RPC.Target.Server)
    public void respawn() {
        Node n = getParent();
        Transform t = n.getTransform();
        UnitComponent u = n.findComponent(UnitComponent.class);

        SpriteRenderer s = new SpriteRenderer();
        s.setTexture("sprites/warrior.png");

        n.addComponent(s);
        n.addComponent(new SimplePlayerComponent());
        n.addComponent(new PlayerSpriteAnimatorComponent());

        u.reset();
        s.setOffset(new Vector2(0, 0.5f));

        t.setPosition(new Vector3(0,0,0));

        n.removeComponent(this);
    }
}
