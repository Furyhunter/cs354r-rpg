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
    Context type();

    boolean validate() default false;
}
