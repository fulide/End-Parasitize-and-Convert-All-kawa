package org.tdddd.epca.impl.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tdddd.epca.impl.overworld.registry.blocks.InfestedBlockInterface;

@OnlyIn(Dist.CLIENT)
@Mixin(LightningBoltRenderer.class)
public class LightningBoltRendererMixin {
    @Unique private static final ThreadLocal<Boolean> IS_PURPLE = ThreadLocal.withInitial(() -> false);

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(LightningBolt entity, float p1, float p2, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        BlockPos below = pos.below();
        boolean isPurple = level.getBlockState(below).getBlock() instanceof InfestedBlockInterface;
        IS_PURPLE.set(isPurple);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(CallbackInfo ci) {
        IS_PURPLE.remove();
    }

    // @WrapOperation instead of @Redirect: redirects are exclusive, so a second mod redirecting the
    // same quad() call (shader/perf mods do this) would turn into a fatal MixinTransformerError.
    // Wrap operations compose, and require = 0 keeps a future mismatch non-fatal.
    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LightningBoltRenderer;quad(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFIFFFFFFFZZZZ)V"
            ),
            require = 0
    )
    private static void redirectQuad(Matrix4f matrix, VertexConsumer consumer,
                                     float x1, float z1, int segment, float x2, float z2,
                                     float r, float g, float b, float offset1, float offset2,
                                     boolean flag1, boolean flag2, boolean flag3, boolean flag4,
                                     Operation<Void> original) {
        if (IS_PURPLE.get()) {
            r = 0.8f;
            g = 0.2f;
            b = 1.0f;
        }
        original.call(matrix, consumer, x1, z1, segment, x2, z2, r, g, b, offset1, offset2,
                flag1, flag2, flag3, flag4);
    }
}
