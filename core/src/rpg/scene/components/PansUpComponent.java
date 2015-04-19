package rpg.scene.components;

import rpg.scene.Scene;
import rpg.scene.replication.Context;
import rpg.scene.systems.NetworkingSceneSystem;

public class PansUpComponent extends Component implements Steppable {
    @Override
    public void step(float deltaTime) {
        Scene s = getParent().getScene();
        NetworkingSceneSystem n = s.findSystem(NetworkingSceneSystem.class);
        if ((n != null && n.getContext() == Context.Server) || n == null) {
            Transform t = getParent().getTransform();
            t.getPosition().add(0, deltaTime / 10, 0);
        }
    }
}
