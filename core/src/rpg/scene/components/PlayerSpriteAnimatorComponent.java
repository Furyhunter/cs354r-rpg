package rpg.scene.components;

import com.badlogic.gdx.math.Vector2;
import rpg.scene.replication.Context;
import rpg.scene.systems.NetworkingSceneSystem;

public class PlayerSpriteAnimatorComponent extends Component implements Steppable {

    private SpriteRenderer spriteRenderer;
    private SimplePlayerComponent playerComponent;

    private int frameX = 1;
    private int frameY = 2;

    private int numFramesX = 3;
    private int numFramesY = 4;

    private float animTime = 0;
    private boolean bounce = false;

    private static float WALKFRAME_LENGTH = .15f;

    @Override
    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);

        if (spriteRenderer == null) {
            spriteRenderer = getParent().findComponent(SpriteRenderer.class);
            spriteRenderer.setDimensions(new Vector2(0.75f, 1f));
            spriteRenderer.texCoordScale = new Vector2(1f / numFramesX, 1f / numFramesY);
        }
        if (playerComponent == null) {
            playerComponent = getParent().findComponent(SimplePlayerComponent.class);
        }

        if (nss == null || nss.getContext() == Context.Client) {
            Vector2 dir = playerComponent.animMoveDirection;
            float angle = 0;

            if (dir.len2() > 0) {
                angle = dir.angle();

                if (angle < (45) && angle + 360 > (45 + 270)) {
                    frameY = 1; // right
                } else if (angle < (45 + 90) && angle > (45)) {
                    frameY = 0; // up
                } else if (angle < (45 + 180) && angle > (45 + 90)) {
                    frameY = 3; // left
                } else if (angle < (45 + 270) && angle > (45 + 180)) {
                    frameY = 2; // down
                }
                animTime += deltaTime;

                if (animTime > WALKFRAME_LENGTH) {
                    animTime = 0;
                    if (frameX == 1) {
                        if (bounce) {
                            frameX = 0;
                        } else {
                            frameX = 2;
                        }
                    } else {
                        frameX = 1;
                        bounce = !bounce;
                    }
                }
            } else {
                animTime = 0;
                frameX = 1;
            }

            spriteRenderer.texCoordTranslation = new Vector2((1f / numFramesX * frameX), (1f / numFramesY * frameY));
        }
    }
}
