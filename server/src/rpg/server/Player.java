package rpg.server;

import com.esotericsoftware.kryonet.Connection;
import rpg.scene.Node;

public class Player {
    public Node possessedNode;
    public Connection kryoConnection;

    public Player() {

    }

    public Player(Node possessedNode, Connection kryoConnection) {
        this.possessedNode = possessedNode;
        this.kryoConnection = kryoConnection;
    }
}
