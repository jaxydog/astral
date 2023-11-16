package dev.jaxydog.mixin.challenge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jaxydog.utility.MobChallengeUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.world.World;

/** Implements the mob challenge system for polar bears */
@Mixin(PolarBearEntity.class)
public abstract class PolarBearEntityMixin extends AnimalEntity implements Angerable {

	protected PolarBearEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
	private void tryAttackInject(Entity target, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (!MobChallengeUtil.shouldScale(this)) {
			return;
		}

		final double base = this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		final double additive = MobChallengeUtil.getAttackAdditive(this.getWorld());
		final double scaled = MobChallengeUtil.getScaledAdditive(this, additive);

		final DamageSource source = this.getDamageSources().mobAttack(this);
		final boolean applyEffects = target.damage(source, (float) (base + scaled));

		if (applyEffects) {
			this.applyDamageEffects(this, target);
		}

		callbackInfo.setReturnValue(applyEffects);
	}

}
