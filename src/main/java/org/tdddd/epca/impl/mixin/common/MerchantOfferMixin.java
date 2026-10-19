package org.tdddd.epca.impl.mixin.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tdddd.epca.impl.overworld.registry.items.item.InfestedEmerald;
import org.tdddd.epca.impl.utils.MerchantOfferMixinAccess;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin implements MerchantOfferMixinAccess {

    @Unique
    private boolean epca$usedInfestedEmerald = false;

    // 1.21.1: MerchantOffer no longer has isRequiredItem; the payment check is satisfiedBy, which
    // tests each cost with ItemCost#test. An infested emerald is accepted wherever an emerald is
    // required, exactly as the 1.20.1 redirect on ItemStack#isSameItem did.
    @Redirect(
            method = "satisfiedBy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/trading/ItemCost;test(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean redirectCostTest(ItemCost cost, ItemStack provided) {
        if (cost.item().value() == Items.EMERALD && provided.getItem() instanceof InfestedEmerald) {
            return true;
        }
        return cost.test(provided);
    }

    @Inject(method = "take", at = @At("RETURN"))
    private void onTakeSuccess(ItemStack firstPayment, ItemStack secondPayment, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            epca$usedInfestedEmerald = (firstPayment.getItem() instanceof InfestedEmerald) || (secondPayment.getItem() instanceof InfestedEmerald);
        } else {
            epca$usedInfestedEmerald = false;
        }
    }

    
    @Unique
    public boolean epca$hasUsedInfestedEmerald() {
        return epca$usedInfestedEmerald;
    }

    
    @Unique
    public void epca$clearInfestedEmeraldFlag() {
        epca$usedInfestedEmerald = false;
    }
}