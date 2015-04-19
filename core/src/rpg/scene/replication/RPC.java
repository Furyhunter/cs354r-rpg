package rpg.scene.replication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an RPC. This will mark it to be added with an RPC ID in the
 * component's replication table.
 * Note: <b>You cannot mark private methods as RPCs.</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RPC {
    Target target();

    boolean validate() default false;

    enum Target {
        /**
         * Executed on the server. Invokable by owning client.
         */
        Server,
        /**
         * Executed on the owning client. Invokable by all, sent only by server.
         */
        Client,
        /**
         * Executed on the server and all clients. Invokable by all, broadcast only by server.
         */
        Multicast
    }
}
