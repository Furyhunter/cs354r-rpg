package rpg;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.esotericsoftware.minlog.Log;
import rpg.client.KryoClientSceneSystem;
import rpg.scene.Scene;
import rpg.scene.systems.GameLogicSystem;
import rpg.scene.systems.RendererSceneSystem;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class App extends ApplicationAdapter {
	Texture img;
	Scene s;
	RendererSceneSystem rendererSceneSystem;
	KryoClientSceneSystem kryoClientSceneSystem;

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
		s.addSystem(kryoClientSceneSystem);
		s.addSystem(new GameLogicSystem());
		s.addSystem(rendererSceneSystem);

		setProjectionMatrix();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		s.update(Gdx.graphics.getRawDeltaTime());
	}

	@Override
	public void resize(int width, int height) {
		setProjectionMatrix();
	}

	private void setProjectionMatrix() {
		float aspect = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
		rendererSceneSystem.getProjectionMatrix().setToOrtho2D(0, 0, 4 * (aspect), 4).translate(2 * aspect, 2, 0);
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
