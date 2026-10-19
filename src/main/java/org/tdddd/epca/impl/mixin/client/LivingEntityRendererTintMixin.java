package org.tdddd.epca.impl.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tdddd.epca.impl.client.ClientColorEffect;


@OnlyIn(Dist.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererTintMixin {

    @Unique
    private static final ThreadLocal<LivingEntity> EPCA_CURRENT_ENTITY = new ThreadLocal<>();

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void epca$captureEntity(LivingEntity entity, float entityYaw, float partialTick,
                                    PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                    CallbackInfo ci) {
        EPCA_CURRENT_ENTITY.set(entity);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void epca$releaseEntity(LivingEntity entity, float entityYaw, float partialTick,
                                    PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                    CallbackInfo ci) {
        EPCA_CURRENT_ENTITY.remove();
    }

    // The model draw call is wrapped instead of redirected:
    //  * a redirect cannot coexist with another mod's redirect on the same invocation (Mixin
    //    reports "@Redirect conflict. Skipping ..." and, with require = 1, turns that into a
    //    fatal MixinTransformerError that takes the whole game down - e.g. in packs shipping
    //    accelerated-rendering / Sodium-class renderers);
    //  * @WrapOperation (MixinExtras, bundled with NeoForge) composes with redirects and with
    //    other wrap operations, so the tint survives alongside performance mods;
    //  * require = 0 makes it fail safe: if a future MC/pack change removes the call, the mod
    //    loses the tint instead of crashing.
    //
    // 1.21.1: Model#renderToBuffer takes a single packed ARGB int instead of four floats, so the
    // tint is unpacked, multiplied and repacked around the wrapped call.
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"),
            require = 0)
    private void epca$renderTintedModel(EntityModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
                                        int packedLight, int packedOverlay, int color,
                                        Operation<Void> original) {
        ClientColorEffect.EffectData data = ClientColorEffect.getEffect(EPCA_CURRENT_ENTITY.get());
        if (data != null) {
            float[] rgb = data.getColorRGB();
            int argb = data.getColorARGB();
            float effectAlpha = ((argb >>> 24) & 0xFF) / 255.0F;

            float red = FastColor.ARGB32.red(color) / 255.0F * rgb[0];
            float green = FastColor.ARGB32.green(color) / 255.0F * rgb[1];
            float blue = FastColor.ARGB32.blue(color) / 255.0F * rgb[2];
            float alpha = FastColor.ARGB32.alpha(color) / 255.0F * effectAlpha;
            color = FastColor.ARGB32.colorFromFloat(alpha, red, green, blue);
        }
        original.call(model, poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
