package rpg.server;

import com.esotericsoftware.kryonet.Connection;
import rpg.scene.Node;

public class Player {
    public Node possessedNode;
    public Connection kryoConnection;

    public int state = AUTHENTICATING;

    public static final int AUTHENTICATING = 0;
    public static final int IN_PLAY = 1;

    public Player() {

    }

    public Player(Node possessedNode, Connection kryoConnection) {
        this.possessedNode = possessedNode;
        this.kryoConnection = kryoConnection;
    }
}
