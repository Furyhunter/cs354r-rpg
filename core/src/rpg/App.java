package rpg;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.Component;
import rpg.scene.components.SpriteRenderer;
import rpg.scene.components.Steppable;
import rpg.scene.components.Transform;
import rpg.scene.systems.GameLogicSystem;
import rpg.scene.systems.RendererSceneSystem;

public class App extends ApplicationAdapter {
	Texture img;
	Scene s;
	RendererSceneSystem rendererSceneSystem;

	class TestC extends Component implements Steppable {

		@Override
		public void step(float deltaTime) {
			Transform t = getParent().findComponent(Transform.class);
			t.getPosition().add(deltaTime / 10f, deltaTime / 10f, 0);
			t.getRotation().mulLeft(new Quaternion(Vector3.Z, deltaTime * 10 * MathUtils.PI));
		}
	}
	@Override
	public void create () {
		img = new Texture(Gdx.files.internal("badlogic.jpg"));
		s = new Scene();
		rendererSceneSystem = new RendererSceneSystem();
		s.addSystem(new GameLogicSystem());
		s.addSystem(rendererSceneSystem);

		Node n = new Node();
		SpriteRenderer sr = new SpriteRenderer();
		sr.setTexture(img);
		n.addComponent(sr);
		s.getRoot().addChild(n);

		n.addComponent(new TestC());

		sr.setDimensions(new Vector2(2, 1));
		sr.setOffset(new Vector2(.5f, .5f));

		rendererSceneSystem.getProjectionMatrix().setToOrtho2D(0, 0, 4 * (4f / 3f), 4);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		float aspect = (float)Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		rendererSceneSystem.getProjectionMatrix().setToOrtho2D(0, 0, 4 * (aspect), 4);
		s.update(Gdx.graphics.getRawDeltaTime());
	}
}
