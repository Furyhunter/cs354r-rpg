package rpg.scene.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;

/**
 * Created by Corin Hill on 5/6/15.
 */
public class SimpleEnemySpawnComponent extends SpawnComponent {
    public SimpleEnemySpawnComponent() {
        frequency = 10;
        maxSpawns = 3;
    }

    @Override
    protected void setFrequency(float frequency) {this.frequency = frequency;}
    @Override
    protected float getFrequency() {return frequency;}
    @Override
    protected void setMaxSpawns(int spawns) {this.maxSpawns = spawns;}
    @Override
    protected int getMaxSpawns(){return maxSpawns;}

    @Override
    protected Node spawn() {
        Node enemyNode = new Node();
        getParent().getScene().getRoot().addChild(enemyNode);

        SimpleEnemyComponent s = new SimpleEnemyComponent();
        SpriteRenderer spriteRenderer = new SpriteRenderer();
        UnitComponent u = new UnitComponent();
        spriteRenderer.setTexture("sprites/enemy-simple.png");
        spriteRenderer.setDimensions(new Vector2(0.5f, 0.5f));
        u.setFaction(UnitComponent.ENEMY);
        enemyNode.addComponent(s);
        enemyNode.addComponent(spriteRenderer);
        enemyNode.addComponent(u);

        Transform tEnemy = enemyNode.getTransform();
        Transform tSelf = getParent().getTransform();

        tEnemy.setPosition(tSelf.getWorldPosition().cpy().add(new Vector3(MathUtils.random(-3f, 3f), MathUtils.random(-3f, 3f), 0)));
        tEnemy.setRotation(tSelf.getWorldRotation());

        return enemyNode;
    }
}
