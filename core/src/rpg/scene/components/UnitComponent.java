package rpg.scene.components;

import com.esotericsoftware.minlog.Log;
import rpg.scene.replication.Replicated;

import java.util.Iterator;
import java.util.List;

public class UnitComponent extends Component implements Hurtable {

    @Replicated
    protected float health = 100;
    @Replicated
    protected float maxHealth = 100;
    @Replicated
    protected float energy = 100;
    @Replicated
    protected float maxEnergy = 100;

    @Replicated
    protected float experience = 0;

    @Replicated
    protected int faction = ENEMY;

    public static final int PLAYER = 0;
    public static final int ENEMY = 1;

    @Override
    public void hurt(Component cause, float baseDamage) {
        // Check if the factions match
        if (cause instanceof SimpleBulletComponent) {
            SimpleBulletComponent bulletComponent = ((SimpleBulletComponent) cause);
            UnitComponent c = bulletComponent.getCreator().findComponent(UnitComponent.class);
            if (c.getFaction() == getFaction()) {
                // Same faction, ignore damage.
                Log.warn(getClass().getSimpleName(), "an attempt to hurt a unit of the same faction was made");
                return;
            }
        }
        if (cause instanceof ExplosionComponent) {
            ExplosionComponent explosionComponent = ((ExplosionComponent) cause);
            UnitComponent c = explosionComponent.getCreator().findComponent(UnitComponent.class);
            if (c.getFaction() == getFaction()) {
                // Same faction, ignore damage.
                Log.warn(getClass().getSimpleName(), "an attempt to hurt a unit of the same faction was made");
                return;
            }
        }

        if (health - baseDamage <= 0) {
            List<Killable> killables = getParent().findComponents(Killable.class);
            Iterator<Killable> itr = killables.iterator();
            while (itr.hasNext()) {
                Killable k = itr.next();
                Component c = ((Component) k);
                if (c.getParent() != null && c.getParent().getParent() != null) {
                    k.kill();
                }
            }
            setHealth(0);
        } else {
            setHealth(getHealth() - baseDamage);
        }
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

    public float getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(float maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public int getFaction() {
        return faction;
    }

    public void setFaction(int faction) {
        this.faction = faction;
    }

    public float getExperience() {
        return experience;
    }

    public void setExperience(float experience) {
        this.experience = experience;
    }
}
