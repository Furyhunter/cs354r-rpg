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
            if (playerComponent.keyW) frameY = 0;
            if (playerComponent.keyD) frameY = 1;
            if (playerComponent.keyS) frameY = 2;
            if (playerComponent.keyA) frameY = 3;

            spriteRenderer.texCoordTranslation = new Vector2((1f / numFramesX * frameX), (1f / numFramesY * frameY));
        }
    }
}
