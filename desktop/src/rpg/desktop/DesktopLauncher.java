package rpg.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import rpg.App;

import java.util.Arrays;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = true;
		config.foregroundFPS = 300;
		config.backgroundFPS = 30;
		config.width = 800;
		config.height = 600;
		App app = new App();
		Arrays.stream(arg).forEach(app::addRunArgument);
		new LwjglApplication(app, config);
	}
}
