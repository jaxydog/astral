package dev.jaxydog.content.power.custom;

import java.util.ArrayList;
import java.util.List;
import dev.jaxydog.content.data.CustomData;
import dev.jaxydog.content.data.MoonPhase;
import dev.jaxydog.content.power.CustomCondition;
import dev.jaxydog.content.power.CustomConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableData.Instance;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

/** The moon phase condition */
public class MoonPhaseCondition extends CustomCondition<Entity> {

	public MoonPhaseCondition(String rawId) {
		super(rawId);
	}

	@Override
	public boolean check(Instance data, Entity value) {
		final MoonPhase phase = data.get("phase");
		final World world = value.getWorld();

		if (phase != MoonPhase.NONE) {
			return phase.isCurrent(world);
		}

		final List<MoonPhase> phases = data.get("phases");

		for (MoonPhase entry : phases) {
			if (entry.isCurrent(world)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public CustomConditionFactory<Entity> factory() {
		final SerializableData data =
				new SerializableData().add("phase", CustomData.MOON_PHASE, MoonPhase.NONE)
						.add("phases", CustomData.MOON_PHASES, new ArrayList<>());

		return new CustomConditionFactory<>(this.getRawId(), data, this::check);
	}

	@Override
	public Registry<ConditionFactory<Entity>> registry() {
		return ApoliRegistries.ENTITY_CONDITION;
	}

}