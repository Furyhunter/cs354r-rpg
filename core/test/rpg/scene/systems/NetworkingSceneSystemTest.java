package rpg.scene.systems;

import org.junit.Test;
import rpg.scene.Node;
import rpg.scene.Scene;
import rpg.scene.components.Component;
import rpg.scene.replication.Context;
import rpg.scene.replication.RPC;
import rpg.scene.replication.RPCMessage;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class NetworkingSceneSystemTest {

    public class TestingNetworkingSceneSystem extends NetworkingSceneSystem {

        @Override
        public Context getContext() {
            return Context.Client;
        }

        @Override
        public void processNode(Node n, float deltaTime) {

        }

        public List<RPCMessage> messages() {
            return rpcMessages;
        }
    }

    public class RPCComponent extends Component {
        @RPC(context = Context.Client)
        public void rpcZeroArgs() {
            testValue = 5;
        }

        @RPC(context = Context.Server)
        public void rpcZeroArgsServer() {
            testValue = 10;
        }

        public int testValue = 0;
    }

    public class SubRPCComponent extends RPCComponent {
        @RPC(context = Context.Client)
        public void rpcZeroArgs() {
            testValue = 15;
        }
    }

    @Test
    public void testSimpleRPC() {
        Scene s = new Scene();
        TestingNetworkingSceneSystem t = new TestingNetworkingSceneSystem();

        Node n = new Node();
        RPCComponent rpcComponent = new RPCComponent();
        n.addComponent(rpcComponent);
        s.getRoot().addChild(n);

        s.addSystem(t);

        rpcComponent.sendRPC("rpcZeroArgs");

        List<RPCMessage> mes = t.messages();

        System.out.println("The component's network ID is: " + rpcComponent.getNetworkID());
        assertEquals("No RPCMessage should be created for equivalent contexts.", 0, mes.size());
        assertEquals("The method should have been invoked, setting a value.", 5, rpcComponent.testValue);
    }

    @Test
    public void testRemoteRPCGeneratesMessage() {
        Scene s = new Scene();
        TestingNetworkingSceneSystem t = new TestingNetworkingSceneSystem();

        Node n = new Node();
        RPCComponent rpcComponent = new RPCComponent();
        n.addComponent(rpcComponent);
        s.getRoot().addChild(n);

        s.addSystem(t);

        rpcComponent.sendRPC("rpcZeroArgsServer");

        List<RPCMessage> mes = t.messages();

        System.out.println("The component's network ID is: " + rpcComponent.getNetworkID());
        assertEquals("An RPCMessage should be created for different contexts.", 1, mes.size());
        assertEquals("The method should NOT have been invoked.", 0, rpcComponent.testValue);
    }

    @Test
    public void testRPCOverride() {
        Scene s = new Scene();
        TestingNetworkingSceneSystem t = new TestingNetworkingSceneSystem();

        Node n = new Node();
        RPCComponent rpcComponent = new SubRPCComponent();
        n.addComponent(rpcComponent);
        s.getRoot().addChild(n);

        s.addSystem(t);

        rpcComponent.sendRPC("rpcZeroArgs");

        List<RPCMessage> mes = t.messages();

        assertEquals(15, rpcComponent.testValue);
    }
}