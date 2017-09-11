package org.egordorichev.lasttry.entity.ai.ais;

import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.entity.CreatureWithAI;
import org.egordorichev.lasttry.entity.ai.AI;
import org.egordorichev.lasttry.entity.ai.AIID;
import org.egordorichev.lasttry.entity.components.CreatureStateComponent;
import org.egordorichev.lasttry.entity.components.PhysicsComponent;
import org.egordorichev.lasttry.injection.CoreRegistry;
import org.egordorichev.lasttry.injection.InjectionHelper;
import org.egordorichev.lasttry.world.biome.BiomeManager;

public class SlimeAI extends AI {
	private static final int MAX = 360;

	private BiomeManager biomeManager;

    public SlimeAI() {
        super(AIID.slime);
		this.biomeManager = CoreRegistry.get(BiomeManager.class);
    }



	@Override
	public void init(CreatureWithAI creature) {
    	creature.ai.setMax(MAX);

	}

	@Override
    public void update(CreatureWithAI creature, int dt, int currentAi) {
		if (currentAi == 0) {
			if (creature.physics.getVelocity().y == 0) {
				creature.physics.jump();
			}

			creature.ai.setMax((int) ((MAX / 2) + (Math.random() * MAX / 2)));

			int dir = Float.compare(Globals.getPlayer().physics.getCenterX(), creature.physics.getCenterX());

			if (dir < 0) {
				creature.ai.setData((short) 0);
			} else if (dir > 0) {
				creature.ai.setData((short) 1);
			}
		}

		if (creature.state.get() == CreatureStateComponent.State.JUMPING || creature.state.get() == CreatureStateComponent.State.FALLING) {
			creature.physics.move((creature.ai.getData() == 0) ? PhysicsComponent.Direction.LEFT : PhysicsComponent.Direction.RIGHT);
		}
    }

	@Override
	public boolean canSpawn() {
		return Globals.environment.time.isDay() && Globals.environment.currentBiome == biomeManager.get("lt:forest");
	}
}