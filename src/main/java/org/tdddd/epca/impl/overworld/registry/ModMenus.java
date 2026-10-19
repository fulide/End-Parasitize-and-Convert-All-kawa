package org.tdddd.epca.impl.overworld.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.tdddd.epca.impl.overworld.registry.blocks.block.entity.SwallowCystBlockEntity;
import org.tdddd.epca.impl.epca;
import org.tdddd.epca.impl.overworld.registry.gui.menus.SwallowCystMenu;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, epca.MODID);

    /**
     * The 1.21.1 replacement for 1.20.1's {@code IForgeMenuType.create(...)}: the factory receives a
     * {@link net.minecraft.network.RegistryFriendlyByteBuf} carrying the extra client-side data written by
     * {@code IMenuProviderExtension#writeClientSideData}.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<SwallowCystMenu>> SWALLOW_CYST =
            MENUS.register("swallow_cyst",
                    () -> IMenuTypeExtension.create((windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        BlockEntity be = inv.player.level().getBlockEntity(pos);
                        if (be instanceof SwallowCystBlockEntity cyst) {
                            return new SwallowCystMenu(windowId, inv, cyst);
                        }
                        return null;
                    }));
}
