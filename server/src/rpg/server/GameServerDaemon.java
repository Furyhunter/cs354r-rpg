package rpg.server;

import com.esotericsoftware.minlog.Log;
import org.cloudcoder.daemon.IDaemon;

public class GameServerDaemon implements IDaemon {

    public GameServerDaemon() {

    }

    @Override
    public void start(String daemonName) {
        Log.info("server", "Starting server daemon " + daemonName);
    }

    @Override
    public void handleCommand(String s) {

    }

    @Override
    public void shutdown() {
        Log.info("server", "Server is shutting down.");
    }

    public static void main(String[] args) {
        GameServerDaemon s = new GameServerDaemon();
        s.start("direct-run");
    }
}
