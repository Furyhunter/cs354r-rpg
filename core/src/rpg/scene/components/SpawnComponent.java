package rpg.scene.components;

import rpg.scene.Node;
import rpg.scene.replication.Context;
import rpg.scene.systems.NetworkingSceneSystem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Corin Hill on 5/6/15.
 */
public abstract class SpawnComponent extends Component implements Steppable {
    private float spawnTimer = 0;
    protected float frequency = 0;

    private Set<Node> spawns = new HashSet<>();
    protected int maxSpawns = 0;

    public void step(float deltaTime) {
        NetworkingSceneSystem nss = getParent().getScene().findSystem(NetworkingSceneSystem.class);
        Objects.requireNonNull(nss);

        if (nss.getContext() == Context.Server) {
            Iterator<Node> i = spawns.iterator();
            while ( i.hasNext() ) {
                Node node = i.next();
                if (node.getParent() == null) {
                    i.remove();
                }
            }
            if (maxSpawns == 0 || spawns.size() < maxSpawns) {
                spawnTimer += deltaTime;
                if (spawnTimer > frequency) {
                    spawns.add(spawn());
                    spawnTimer = 0;
                }
            }
        }
    }

    protected abstract Node spawn();

}
