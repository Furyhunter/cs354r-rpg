package rpg.scene.components;

import rpg.scene.replication.Context;
import rpg.scene.systems.NetworkingSceneSystem;

import java.util.Objects;

/**
 * Created by Corin Hill on 5/6/15.
 */
public abstract class SpawnComponent extends Component implements Steppable {
    private float spawnTimer = 0;
    protected float frequency = 0;

    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Objects.requireNonNull(nss);

        if (nss.getContext() == Context.Server) {
            spawnTimer += deltaTime;
            if (spawnTimer > getFrequency()) {
                spawn();
                spawnTimer = 0;
            }
        }
    }
    protected abstract void setFrequency(float frequency);
    protected abstract float getFrequency();
    protected abstract void spawn();

}
