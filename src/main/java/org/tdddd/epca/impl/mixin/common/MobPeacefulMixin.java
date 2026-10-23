package org.tdddd.epca.impl.mixin.common;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tdddd.epca.impl.overworld.registry.entities.IInfested;
import org.tdddd.epca.impl.overworld.registry.entities.IParasite;

/**
 * 让**所有**虫族生物在和平难度下像原版敌对生物一样被清除。
 *
 * <p>本模组大多数实体直接 {@code extends PathfinderMob}（Ripper、虫染史莱姆、Walking*Head……），
 * 并不继承 {@code AbstractEpcaEntity}，所以没法靠基类覆写；这里在 {@code Mob} 层面统一接管：
 * 只要是虫族接口的实现，{@code shouldDespawnInPeaceful()} 就返回 true，
 * 随后原版 {@code ServerLevel} 的 {@code checkDespawn()} 会把它 discard 掉。</p>
 */
@Mixin(Mob.class)
public abstract class MobPeacefulMixin {

    @Inject(method = "shouldDespawnInPeaceful", at = @At("HEAD"), cancellable = true)
    private void epca$shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self instanceof IParasite || self instanceof IInfested) {
            cir.setReturnValue(true);
        }
    }
}
