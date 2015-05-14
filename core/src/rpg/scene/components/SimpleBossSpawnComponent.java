package rpg.scene.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;

/**
 * Created by Corin Hill on 5/14/15.
 */
public class SimpleBossSpawnComponent extends SpawnComponent{
    public SimpleBossSpawnComponent() {
        frequency = 60;
        maxSpawns = 1;
    }

    public Node spawn() {
        Node node = new Node();
        getParent().findRoot().addChild(node);

        SimpleBossComponent boss = new SimpleBossComponent();

        SpriteRenderer sr = new SpriteRenderer();
        sr.setTexture("sprites/enemy-simple.png");
        sr.setDimensions(new Vector2(5f, 5f));
        sr.setOffset(new Vector2(0, 0.5f));

        UnitComponent unit = new UnitComponent();
        unit.setFaction(UnitComponent.ENEMY);
        unit.setMaxHealth(1000);
        unit.setHealth(unit.getMaxHealth());

        node.addComponent(boss);
        node.addComponent(sr);
        node.addComponent(unit);

        Transform tEnemy = node.getTransform();
        Transform tSelf = getParent().getTransform();
        tEnemy.setPosition(tSelf.getWorldPosition().cpy()
                .add(new Vector3(MathUtils.random(-3f, 3f), MathUtils.random(-3f, 3f), 0)));
        tEnemy.setRotation(tSelf.getWorldRotation());

        return node;
    }
}
