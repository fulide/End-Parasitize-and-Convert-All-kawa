package org.tdddd.epca.impl.overworld.registry.items.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.tdddd.epca.impl.epca;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.tdddd.epca.impl.overworld.registry.entities.entity.misc.ThrownWoodenSpear;

public class WoodenSpear extends Item {
    private final ItemAttributeModifiers defaultModifiers;

    public static final int MAX_CHARGE = 12000; 
    public static final int FULL_CHARGE_TICKS = 20; 
    public static final float MAX_SPEED = 2.5F; 
    public static final float MIN_SPEED = 1.2F; 

    public WoodenSpear(Properties properties) {
        super(properties);
        // 1.21.1: attribute modifiers are an ItemAttributeModifiers value with Holder<Attribute> keys,
        // ResourceLocation ids and EquipmentSlotGroup slots. ForgeMod.ENTITY_REACH has no NeoForge 21.1
        // equivalent; its vanilla successor is Attributes.ENTITY_INTERACTION_RANGE.
        this.defaultModifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.8, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(ResourceLocation.fromNamespaceAndPath(epca.MODID, "weapon_reach"), 1.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return this.defaultModifiers;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_CHARGE;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR; 
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof Player player)) return;
        int charge = this.getUseDuration(stack, livingEntity) - timeCharged;
        
        int effectiveCharge = Math.min(charge, FULL_CHARGE_TICKS);
        float chargePercent = (float) effectiveCharge / FULL_CHARGE_TICKS;
        float speed = MIN_SPEED + (MAX_SPEED - MIN_SPEED) * chargePercent;
        if (speed < 0.1F) return;

        if (!level.isClientSide) {
            boolean isCreative = player.getAbilities().instabuild;
            ThrownWoodenSpear spear = new ThrownWoodenSpear(level, player, stack, isCreative);
            spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, 1.0F);
            if (charge >= FULL_CHARGE_TICKS) { 
                spear.setCritArrow(true);
            }
            level.addFreshEntity(spear);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
    // 1.21.1 REMOVED: canApplyAtEnchantingTable(ItemStack, Enchantment) and EnchantmentCategory no longer exist.
    // Enchanting is data driven; the enchanting table offers the enchantments whose
    // EnchantmentDefinition#supportedItems tag contains this item. To keep the 1.20.1 list (mending, unbreaking,
    // sharpness, smite, bane_of_arthropods, fire_aspect, loyalty) the item must be added to the corresponding
    // minecraft:enchantable/* item tags through a data pack. See the port report.

    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && !player.getAbilities().instabuild) {
            
            stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        
        if (miningEntity instanceof Player player && !player.getAbilities().instabuild) {
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness > 0.0F) {
                stack.hurtAndBreak(1, miningEntity, EquipmentSlot.MAINHAND);
            }
        }
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 200; 
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemTags.PLANKS);
    }
}