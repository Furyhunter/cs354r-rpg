package rpg.server;

import org.cloudcoder.daemon.DaemonController;
import org.cloudcoder.daemon.IDaemon;

public class GameServerDaemonController extends DaemonController {
    @Override
    public String getDefaultInstanceName() {
        return "bulletHellRPGServer";
    }

    @Override
    public Class<? extends IDaemon> getDaemonClass() {
        return GameServerDaemon.class;
    }

    public static void main(String[] args) {
        GameServerDaemonController c = new GameServerDaemonController();
        c.exec(args);
    }
}
