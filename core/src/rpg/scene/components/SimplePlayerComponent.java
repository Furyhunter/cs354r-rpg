package rpg.scene.components;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPC;
import rpg.scene.systems.InputSystem.InputEvent;
import rpg.scene.systems.NetworkingSceneSystem;

import static rpg.scene.systems.InputSystem.EventType.KeyDown;
import static rpg.scene.systems.InputSystem.EventType.KeyUp;

public class SimplePlayerComponent extends Component implements Steppable, InputEventListener {

    private boolean keyW;
    private boolean keyS;
    private boolean keyA;
    private boolean keyD;

    private static float MOVE_SPEED = 4;
    private static float MOVE_SPEED_SQUARED = 16;

    private float moveTimer = 0;
    private static float MOVE_UPDATE_THRESHOLD = 1.f / 30;

    private Vector3 oldPosition;
    private Vector3 newPosition;

    private Vector3 clientRealPosition = null;

    private boolean lerpTargetChanged = false;

    @Override
    public void processInputEvent(InputEvent event) {
        if (event.getType() == KeyDown) {
            switch (event.getButton()) {
                case Keys.W:
                    keyW = true;
                    break;
                case Keys.S:
                    keyS = true;
                    break;
                case Keys.A:
                    keyA = true;
                    break;
                case Keys.D:
                    keyD = true;
                    break;
                default:
                    break;
            }
        }
        if (event.getType() == KeyUp) {
            switch (event.getButton()) {
                case Keys.W:
                    keyW = false;
                    break;
                case Keys.S:
                    keyS = false;
                    break;
                case Keys.A:
                    keyA = false;
                    break;
                case Keys.D:
                    keyD = false;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        if (nss == null || (nss.getContext() == Context.Client && getParent().isPossessed())) {
            Transform t = getParent().getTransform();
            if (clientRealPosition == null) {
                clientRealPosition = t.getPosition();
            }
            moveTimer += deltaTime;

            // Handle movement
            Vector3 moveDirection = new Vector3();
            if (keyW) {
                moveDirection.y += 1;
            }
            if (keyS) {
                moveDirection.y -= 1;
            }
            if (keyA) {
                moveDirection.x -= 1;
            }
            if (keyD) {
                moveDirection.x += 1;
            }

            moveDirection.nor().scl(MOVE_SPEED).scl(deltaTime);
            clientRealPosition.add(moveDirection);
            if (moveTimer >= MOVE_UPDATE_THRESHOLD) {
                sendRPC("setPlayerPosition", clientRealPosition.cpy());
                moveTimer = 0;
            }
            t.setPosition(clientRealPosition);

        } else if (nss.getContext() == Context.Server) {
            // Server side
            Transform t = getParent().getTransform();

            // For interpolating movement from the client
            // The client decides what position it is in, rather than the server
            moveTimer += deltaTime;
            if (oldPosition != null) {
                if (moveTimer >= MOVE_UPDATE_THRESHOLD) {
                    oldPosition = null;
                    moveTimer = 0;
                    t.setPosition(newPosition.cpy());
                } else {
                    t.setPosition(oldPosition.cpy().lerp(newPosition, moveTimer / MOVE_UPDATE_THRESHOLD));
                }
            } else if (newPosition != null) {
                t.setPosition(newPosition);
            }
        } else if (nss.getContext() == Context.Client) {
            Transform t = getParent().getTransform();

            if (oldPosition != null) {
                moveTimer += deltaTime;
                if (lerpTargetChanged) {
                    moveTimer = 0;
                    lerpTargetChanged = false;
                }

                t.setPosition(oldPosition.cpy().lerp(newPosition, moveTimer / nss.getTickDeltaTime()));
            } else if (newPosition != null) {
                t.setPosition(newPosition);
            }
        }
    }

    @RPC(target = RPC.Target.Server)
    public void setPlayerPosition(Vector3 vec) {
        Transform t = getParent().getTransform();
        vec.z = 0;
        if (vec.cpy().sub(t.getPosition()).len2() > MOVE_SPEED_SQUARED * 1.5f) {
            Log.warn(getClass().getSimpleName(), "Player tried to move too fast.");
        }
        oldPosition = t.getPosition().cpy();
        newPosition = vec.cpy();
        moveTimer = 0;
    }

    @Override
    public void onPreApplyReplicateFields() {
        Transform t = getParent().getTransform();
        oldPosition = newPosition == null ? null : newPosition.cpy();
        // The transform has already been updated by now.
        newPosition = t.getPosition().cpy();
        lerpTargetChanged = true;
    }

    @Override
    public boolean isAlwaysFieldReplicated() {
        return true;
    }
}
