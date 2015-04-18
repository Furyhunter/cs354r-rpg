package rpg.server;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.esotericsoftware.minlog.Log;
import org.cloudcoder.daemon.IDaemon;
import rpg.scene.Scene;
import rpg.scene.systems.GameLogicSystem;

import java.io.IOException;

public class GameServerDaemon implements IDaemon, ApplicationListener {

    private Scene s;
    private HeadlessApplication app;

    private GameLogicSystem gameLogicSystem;
    private KryoServerSceneSystem kryoServerSceneSystem;

    public GameServerDaemon() {

    }

    @Override
    public void start(String daemonName) {
        Log.info("server", "Starting server daemon " + daemonName);
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.renderInterval = 1f / 60f;
        app = new HeadlessApplication(this, config);
    }

    @Override
    public void handleCommand(String s) {

    }

    @Override
    public void shutdown() {
        Log.info("server", "Server is shutting down.");
        app.exit();
    }

    public static void main(String[] args) {
        GameServerDaemon s = new GameServerDaemon();
        s.start("direct-run");
    }

    @Override
    public void create() {
        s = new Scene();
        gameLogicSystem = new GameLogicSystem();
        s.addSystem(gameLogicSystem);
        try {
            kryoServerSceneSystem = new KryoServerSceneSystem();
            s.addSystem(kryoServerSceneSystem);
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        s.update(Gdx.graphics.getRawDeltaTime());
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
