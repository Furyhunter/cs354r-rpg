package rpg.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import rpg.scene.Node;
import rpg.scene.kryo.BeginTick;
import rpg.scene.kryo.EndTick;
import rpg.scene.kryo.KryoClassRegisterUtil;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPCMessage;
import rpg.scene.systems.NetworkingSceneSystem;

import java.io.IOException;
import java.net.InetAddress;

public class KryoClientSceneSystem extends NetworkingSceneSystem {

    private Client client;
    private InetAddress hostAddress;

    private boolean connecting = false;

    public void setHostAddress(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    class ClientListener extends Listener {
        @Override
        public void connected(Connection connection) {
            Log.info(getClass().getSimpleName(), "Connected");
        }

        @Override
        public void disconnected(Connection connection) {
            Log.info(getClass().getSimpleName(), "Disconnected");
        }

        @Override
        public void received(Connection connection, Object o) {
            if (o instanceof BeginTick || o instanceof EndTick) {
                return;
            }
            Log.info(getClass().getSimpleName(), "Received " + o);
        }
    }

    public KryoClientSceneSystem() throws IOException {
        client = new Client();
        KryoClassRegisterUtil.registerAll(client.getKryo());
        client.addListener(new ClientListener());
    }

    @Override
    public Context getContext() {
        return Context.Client;
    }

    @Override
    public void processRPC(RPCMessage m) {

    }

    @Override
    public void processMulticastRPC(RPCMessage m) {

    }

    @Override
    public boolean canProcessRPCs() {
        return false;
    }

    @Override
    public void processNode(Node n, float deltaTime) {

    }

    @Override
    public void beginProcessing() {
        if (!connecting) {
            client.start();
            new Thread(() -> {
                try {
                    client.connect(5000, hostAddress, 31425, 31426);
                } catch (IOException e) {
                    Log.error(KryoClientSceneSystem.class.getSimpleName(), e);
                }
            }).start();
            connecting = true;
        }
    }

    @Override
    public boolean doesProcessNodes() {
        return false;
    }
}
