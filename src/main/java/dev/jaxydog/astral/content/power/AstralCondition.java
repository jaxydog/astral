package dev.jaxydog.astral.content.power;

import dev.jaxydog.astral.register.Registered;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData.Instance;
import net.minecraft.registry.Registry;

/** Abstract class for implementing conditions */
public abstract class AstralCondition<T> implements Registered.Common {

    /** The custom condition's inner raw identifier */
    private final String RAW_ID;

    public AstralCondition(String rawId) {
        this.RAW_ID = rawId;
    }

    /** Checks the condition */
    public abstract boolean check(Instance data, T value);

    @Override
    public String getRegistryPath() {
        return this.RAW_ID;
    }

    @Override
    public void registerCommon() {
        this.factory().register(this.registry());
    }

    /** Returns the condition's factory */
    public abstract AstralConditionFactory<T> factory();

    /** Returns the action's registry */
    public abstract Registry<ConditionFactory<T>> registry();

}