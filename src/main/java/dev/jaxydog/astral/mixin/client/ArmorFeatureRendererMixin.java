package dev.jaxydog.astral.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.jaxydog.astral.content.item.AstralArmorItem;
import dev.jaxydog.astral.content.item.Colored;
import dev.jaxydog.astral.content.trinket.AstralTrinketPredicates;
import dev.jaxydog.astral.utility.color.Rgb;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Implements multiple texture layers and coloring for custom armor items */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {

    public ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Shadow
    protected abstract boolean usesInnerModel(EquipmentSlot slot);

    @Shadow
    protected abstract void renderArmorParts(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        ArmorItem item,
        A model,
        boolean secondTextureLayer,
        float red,
        float green,
        float blue,
        @org.jetbrains.annotations.Nullable String overlay
    );

    @Shadow
    protected abstract void renderTrim(
        ArmorMaterial material,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        ArmorTrim trim,
        A model,
        boolean leggings
    );

    @Shadow
    protected abstract void renderGlint(
        MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model
    );

    @Shadow
    protected abstract void setVisible(A bipedModel, EquipmentSlot slot);

    @ModifyVariable(method = "renderArmor", at = @At("STORE"))
    private ItemStack cosmeticReplacer(ItemStack equippedStack, @Local(argsOnly = true) T entity) {
        final ItemStack cosmeticStack = AstralTrinketPredicates.getCosmeticHelmet(entity);

        if (cosmeticStack.isEmpty() || equippedStack.isIn(AstralTrinketPredicates.COSMETIC_HELMET_UNHIDEABLE)) {
            return equippedStack;
        } else {
            return cosmeticStack;
        }
    }

    @SuppressWarnings({ "unchecked", "UnstableApiUsage" })
    @Inject(
        method = "renderArmor", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;usesInnerModel(Lnet/minecraft/entity/EquipmentSlot;)Z"
    ), cancellable = true
    )
    private void customOverwrite(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        T entity,
        EquipmentSlot armorSlot,
        int light,
        A model,
        CallbackInfo callbackInfo,
        @Local ItemStack stack,
        @Local ArmorItem baseArmorItem
    ) {
        if (!(baseArmorItem instanceof final AstralArmorItem armorItem)) return;

        final ArmorRenderer renderer = ArmorRendererRegistryImpl.get(armorItem);

        if (renderer != null) {
            final BipedEntityModel<LivingEntity> customModel = (BipedEntityModel<LivingEntity>) this.getContextModel();

            renderer.render(matrices, vertexConsumers, stack, entity, armorSlot, light, customModel);
            callbackInfo.cancel();

            return;
        }

        final boolean useInner = this.usesInnerModel(armorSlot);
        final int layers = armorItem.getArmorTextureLayers(stack);

        for (int layer = 0; layer < layers; layer += 1) {
            final String overlay = String.valueOf(layer);

            if (armorItem instanceof final Colored colored) {
                final Rgb color = new Rgb(colored.getStackColor(stack, layer));

                final float r = (float) color.redScaled();
                final float g = (float) color.greenScaled();
                final float b = (float) color.blueScaled();

                this.renderArmorParts(matrices, vertexConsumers, light, armorItem, model, useInner, r, g, b, overlay);
            } else {
                this.renderArmorParts(matrices, vertexConsumers, light, armorItem, model, useInner, 1, 1, 1, overlay);
            }
        }

        ArmorTrim.getTrim(entity.getWorld().getRegistryManager(), stack).ifPresent(armorTrim -> {
            final ArmorMaterial material = armorItem.getMaterial();

            this.renderTrim(material, matrices, vertexConsumers, light, armorTrim, model, useInner);
        });

        if (stack.hasGlint()) {
            this.renderGlint(matrices, vertexConsumers, light, model);
        }

        callbackInfo.cancel();
    }

}
