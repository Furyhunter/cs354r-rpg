package rpg.server;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.minlog.Log;
import org.cloudcoder.daemon.IDaemon;
import rpg.game.OpenSimplexNoise;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.Component;
import rpg.scene.components.SimpleEnemySpawnComponent;
import rpg.scene.components.TilemapRendererComponent;
import rpg.scene.replication.RepTable;
import rpg.scene.systems.GameLogicSystem;
import rpg.scene.systems.GdxAssetManagerSystem;
import rpg.scene.systems.Node2DQuerySystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class GameServerDaemon implements IDaemon, ApplicationListener {

    private Scene s;
    private HeadlessApplication app;

    private GameLogicSystem gameLogicSystem;
    private KryoServerSceneSystem kryoServerSceneSystem;
    private Node2DQuerySystem querySystem;

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
        RepTable.scene = s;
        gameLogicSystem = new GameLogicSystem();
        s.addSystem(GdxAssetManagerSystem.getSingleton());
        s.addSystem(gameLogicSystem);
        querySystem = new Node2DQuerySystem();
        s.addSystem(querySystem);
        try {
            kryoServerSceneSystem = new KryoServerSceneSystem();
            s.addSystem(kryoServerSceneSystem);
            kryoServerSceneSystem.setRelevantSetDecider(new DistanceRelevantDecider(querySystem, 15));
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }

        // Random map generation.
        generateRandomWorld(System.currentTimeMillis());
    }

    private void generateRandomWorld(long seed) {
        Random r = new Random(seed);
        OpenSimplexNoise noise = new OpenSimplexNoise(r.nextLong());
        OpenSimplexNoise noiseBig = new OpenSimplexNoise(r.nextLong());
        OpenSimplexNoise noiseSmall = new OpenSimplexNoise(r.nextLong());

        for (int iix = -5; iix <= 5; iix++) {
            for (int iiy = -5; iiy <= 5; iiy++) {
                Node n = new Node();
                int width = 32;
                int height = 32;
                final int finalIIX = iix;
                final int finalIIY = iiy;
                double scaleFactor = 14;
                TilemapRendererComponent tilemapRendererComponent = new TilemapRendererComponent(width, height);
                s.getRoot().addChild(n);
                IntStream.range(0, width + 1).forEach(ix -> {
                            IntStream.range(0, height + 1).forEach(iy -> {
                                double noiseX = ((double) (ix + (finalIIX * width)) / (width * 7) * scaleFactor);
                                double noiseY = ((double) (iy + (finalIIY * height)) / (height * 7) * scaleFactor);
                                float value = (float) noise.eval(noiseX, noiseY) * .8f;
                                value += noiseBig.eval(noiseX / 20, noiseY / 20) * 1.5f;
                                value += noiseSmall.eval(noiseX * 10, noiseY * 10) * .1f;
                                value = MathUtils.clamp(value, -1, 1);
                                tilemapRendererComponent.setPointValue(ix, iy, (value + 1) / 2.f);
                            });
                        }
                );
                n.addComponent(tilemapRendererComponent);
                n.setStaticReplicant(true);

                tilemapRendererComponent.generateRandomDoodads();

                n.getTransform().translate(iix * width, iiy * height, 0);

                if (iix > -5 && iix < 5 && iiy > -5 && iiy < 5) {
                    Node localSpawnNode = Node.createLocalNode();
                    s.getRoot().addChild(localSpawnNode);
                    localSpawnNode.addComponent(Component.createLocalComponent(SimpleEnemySpawnComponent.class));
                    localSpawnNode.getTransform().translate(iix * width, iiy * height, 0);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        s.update(Gdx.graphics.getDeltaTime());
        if (Gdx.graphics.getDeltaTime() > (1.f / kryoServerSceneSystem.getReplicationRate())) {
            Log.warn(getClass().getSimpleName(), "Network tick time overrun! "
                    + Gdx.graphics.getDeltaTime() + "s to evaluate frame on frame " + Gdx.graphics.getFrameId());
        }
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
