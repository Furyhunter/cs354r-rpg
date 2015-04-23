package rpg;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import rpg.client.KryoClientSceneSystem;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.RectangleRenderer;
import rpg.scene.systems.*;
import rpg.ui.InGameUIScreen;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class App extends ApplicationAdapter {
	Texture img;
	Scene s;
	RendererSceneSystem rendererSceneSystem;
	KryoClientSceneSystem kryoClientSceneSystem;
	InputSystem inputSystem;
	Scene2DUISystem uiSystem;

	private List<String> runArguments = new ArrayList<>();

	private InetAddress host;

	@Override
	public void create() {
		// Process run arguments
		if (!runArguments.contains("--local")) {
			initializeHostName();
		}

		s = new Scene();
		rendererSceneSystem = new RendererSceneSystem();
		if (host != null) {
			try {
				kryoClientSceneSystem = new KryoClientSceneSystem();
				kryoClientSceneSystem.setHostAddress(host);
			} catch (IOException e) {
				e.printStackTrace();
				Gdx.app.exit();
			}
		}
		inputSystem = new InputSystem();
		uiSystem = new Scene2DUISystem();
		if (!runArguments.contains("--local")) s.addSystem(kryoClientSceneSystem);
		s.addSystem(GdxAssetManagerSystem.getSingleton());
		s.addSystem(inputSystem);
		s.addSystem(new GameLogicSystem());
		s.addSystem(rendererSceneSystem);
		s.addSystem(uiSystem);
		uiSystem.setScreen(new InGameUIScreen());

		if (runArguments.contains("--local")) {
			// LOCAL TESTING CODE
			IntStream.range(0, 100).forEach(i -> {
				Node n = new Node();
				RectangleRenderer r = new RectangleRenderer();
				s.getRoot().addChild(n);

				r.setTransparent(true);
				r.setSize(new Vector2(0.5f, 0.5f));
				r.setColor(new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), MathUtils.random()));
				n.addComponent(r);
				n.getTransform().setPosition(new Vector3(MathUtils.random() * 2 - 1, MathUtils.random() * 2 - 1, 0));
			});
			IntStream.range(0, 100).forEach(i -> {
				Node n = new Node();
				RectangleRenderer r = new RectangleRenderer();
				s.getRoot().addChild(n);

				r.setTransparent(false);
				r.setSize(new Vector2(0.3f, 0.3f));
				r.setColor(new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), MathUtils.random()));
				n.addComponent(r);
				n.getTransform().setPosition(new Vector3(MathUtils.random() * 2 - 1, MathUtils.random() * 2 - 1, 0));
			});
		}

		setProjectionMatrix();
	}

	@Override
	public void render() {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		s.update(Gdx.graphics.getRawDeltaTime());
		//Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + "fps");
	}

	@Override
	public void resize(int width, int height) {
		setProjectionMatrix();
	}

	private void setProjectionMatrix() {
		float aspect = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
		rendererSceneSystem.getProjectionMatrix().setToOrtho2D(0, 0, 8 * (aspect), 8).translate(4 * aspect, 4, 0);
	}

	public List<String> getRunArguments() {
		return runArguments;
	}

	public void addRunArgument(String arg) {
		runArguments.add(arg);
	}

	private void initializeHostName() {
		int argIndex = 0;
		if ((argIndex = runArguments.indexOf("--host")) != -1 && runArguments.size() > argIndex + 1) {
			try {
				host = InetAddress.getByName(runArguments.get(argIndex + 1));
			} catch (IOException e) {
				Log.warn(getClass().getSimpleName(),
						"Unable to resolve host " + runArguments.get(argIndex + 1) + ", falling back to idolagames.",
						e);
			}
		}

		if (host == null) {
			try {
				host = InetAddress.getByName("idolagames.com");
			} catch (IOException e) {
				Log.error(getClass().getSimpleName(), "Unable to resolve host idolagames.com, cannot connect to server.");
			}
		}
	}
}
