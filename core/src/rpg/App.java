package rpg;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.SpriteRenderer;
import rpg.scene.components.Transform;
import rpg.scene.systems.GameLogicSystem;
import rpg.scene.systems.RendererSceneSystem;

public class App extends ApplicationAdapter {
	Texture img;
	Scene s;
	
	@Override
	public void create () {
		img = new Texture(Gdx.files.internal("badlogic.jpg"));
		s = new Scene();
		RendererSceneSystem rendererSceneSystem = new RendererSceneSystem();
		s.addSystem(new GameLogicSystem());
		s.addSystem(rendererSceneSystem);

		Node n = new Node();
		SpriteRenderer sr = new SpriteRenderer();
		sr.setTexture(img);
		n.addComponent(sr);
		s.getRoot().addChild(n);

		rendererSceneSystem.getProjectionMatrix().setToOrtho2D(-1, -1, 1, 1, .001f, 100f);

		n.findComponent(Transform.class).setPosition(new Vector3(0, 0, 0));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		s.update(Gdx.graphics.getRawDeltaTime());
	}
}
