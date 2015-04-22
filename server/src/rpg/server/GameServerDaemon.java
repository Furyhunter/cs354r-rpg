package rpg.server;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.esotericsoftware.minlog.Log;
import org.cloudcoder.daemon.IDaemon;
import rpg.Diagnostics;
import rpg.scene.Scene;
import rpg.scene.systems.GameLogicSystem;
import rpg.scene.systems.GdxAssetManagerSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.TreeMap;

public class GameServerDaemon implements IDaemon, ApplicationListener {

    private Scene s;
    private HeadlessApplication app;

    private GameLogicSystem gameLogicSystem;
    private KryoServerSceneSystem kryoServerSceneSystem;

    private int frames = 0;
    private Map<String, Long> totalTimes = new TreeMap<>();

    public GameServerDaemon() {

    }

    @Override
    public void start(String daemonName) {
        Log.setLogger(new Log.Logger() {
            private PrintStream printStream = null;

            @Override
            protected void print(String message) {
                if (printStream == null) {
                    try {
                        printStream = new PrintStream(new FileOutputStream(new File("log-" + daemonName + ".txt")));
                    } catch (IOException e) {
                        printStream = System.out;
                    }
                }
                printStream.println(message);
                if (printStream != System.out) {
                    System.out.println(message);
                }
            }

            @Override
            public void log(int level, String category, String message, Throwable ex) {
                String logLevel;
                switch (level) {
                    case Log.LEVEL_DEBUG:
                        logLevel = "DEBUG";
                        break;
                    case Log.LEVEL_ERROR:
                        logLevel = "ERROR";
                        break;
                    case Log.LEVEL_INFO:
                        logLevel = " INFO";
                        break;
                    case Log.LEVEL_TRACE:
                        logLevel = "TRACE";
                        break;
                    case Log.LEVEL_WARN:
                        logLevel = " WARN";
                        break;
                    case Log.LEVEL_NONE:
                        logLevel = "     ";
                        break;
                    default:
                        logLevel = "UNKNO";
                        break;
                }
                StringBuilder b = new StringBuilder();
                b.append(LocalDateTime.now(Clock.systemDefaultZone()).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
                b.append(" ");
                b.append(logLevel);
                b.append(": [");
                b.append(category);
                b.append("] ");
                b.append(message);
                if (ex != null) {
                    b.append("\n");
                    b.append(ex.toString());
                }

                print(b.toString());
            }
        });
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
        s.addSystem(GdxAssetManagerSystem.getSingleton());
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
        s.update(Gdx.graphics.getDeltaTime());
        Diagnostics.resetTimes();
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
