package rpg.scene.components;

public class DetachAfterDelayComponent extends Component implements Steppable {

    public float timeRemaining = 5;

    public DetachAfterDelayComponent() {

    }

    public DetachAfterDelayComponent(float time) {
        timeRemaining = time;
    }

    @Override
    public void step(float deltaTime) {
        timeRemaining -= deltaTime;

        if (timeRemaining < 0) {
            getParent().removeFromParent();
        }
    }
}
