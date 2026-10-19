package org.tdddd.epca.impl.overworld.registry.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.tdddd.epca.impl.client.GuiTexture;

/**
 * Creative tab with a slot grid the mod can decorate (subheading rows are drawn by
 * {@code org.tdddd.epca.impl.client.CustomTabRenderer}).
 *
 * <p><b>1.21.1 change:</b> {@link CreativeModeTab}'s {@code displayItems}, {@code displayItemsSearchTab} and
 * {@code rebuildSearchTree()} became private / were removed, so a subclass can no longer assign the built contents.
 * {@link #buildContents(CreativeModeTab.ItemDisplayParameters)} therefore stores the generated stacks in this class and
 * the public read hooks ({@link #getDisplayItems()}, {@link #getSearchTabDisplayItems()}, {@link #contains(ItemStack)},
 * {@link #hasAnyItems()}) are overridden. That is also why the placeholder {@code ItemStack.EMPTY} cells (used for the
 * 9-wide layout) are still possible: vanilla's own {@code Output#accept} rejects stacks whose count is not exactly 1.</p>
 */
public class CustomTab extends CreativeModeTab {

    public final ImmutableList<ITabEntry> entries;
    
    public final Map<Integer, ITabEntry> renderedEntries;

    /** Contents of the parent tab (may contain {@link ItemStack#EMPTY} layout placeholders). */
    private List<ItemStack> customDisplayItems = Collections.emptyList();
    /** Contents of the search tab. */
    private List<ItemStack> customSearchItems = Collections.emptyList();

    protected CustomTab(Builder builder) {
        super(builder);
        entries = ImmutableList.copyOf(builder.entries);
        renderedEntries = new HashMap<>();
    }

    @Override
    public void buildContents(ItemDisplayParameters parameters) {
        List<ItemStack> items = new ArrayList<>(entries.size());
        List<ItemStack> searchItems = new ArrayList<>(entries.size());
        renderedEntries.clear();
        for (ITabEntry entry : entries) {
            entry.addItems(items, parameters, i -> renderedEntries.put(i, entry));
            searchItems.addAll(entry.getItemsToAddToSearch(parameters));
        }
        customDisplayItems = items;
        customSearchItems = searchItems;
    }

    @Override
    public Collection<ItemStack> getDisplayItems() {
        return customDisplayItems;
    }

    @Override
    public Collection<ItemStack> getSearchTabDisplayItems() {
        return customSearchItems;
    }

    @Override
    public boolean hasAnyItems() {
        return !customDisplayItems.isEmpty();
    }

    @Override
    public boolean contains(ItemStack stack) {
        for (ItemStack candidate : customSearchItems) {
            if (ItemStack.isSameItemSameComponents(candidate, stack)) {
                return true;
            }
        }
        return false;
    }

    public static class Builder extends CreativeModeTab.Builder {

        public final List<ITabEntry> entries = new ArrayList<>();

        public Builder(Row row, int column) {
            super(row, column);
            withTabFactory(b -> {
                if (!(b instanceof Builder cb)) throw new IllegalArgumentException("Supplied builder must extend CustomTab.Builder");
                return new CustomTab(cb);
            });
        }

        public Builder add(ITabEntry ...entries) {
            for (ITabEntry entry : entries) this.entries.add(entry);
            return this;
        }

        
        @Override
        @Deprecated
        public Builder displayItems(DisplayItemsGenerator displayItemsGenerator) {
            return this;
        }

    }

    public static interface ITabEntry {
        
        public default void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {};

        public default int getSize() {
            return 1;
        };

        
        public default boolean newLine() {
            return false;
        };

        
        public default void addItems(List<ItemStack> stacks, ItemDisplayParameters parameters, IntConsumer specialRenderLocation) {
            if (newLine()) stacks.addAll(Collections.nCopies(((9 - (stacks.size() % 9)) % 9), ItemStack.EMPTY));
            if (hasSpecialRendering()) specialRenderLocation.accept(stacks.size());
            stacks.addAll(Collections.nCopies(getSize(), ItemStack.EMPTY));
        };

        public default boolean hasSpecialRendering() {
            return false;
        };

        public default void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {};

        public default Collection<ItemStack> getItemsToAddToSearch(ItemDisplayParameters parameters) {
            return Collections.emptyList();
        };

        public static final ITabEntry EMPTY = new ITabEntry() {};

        public static final ITabEntry LINE_BREAK = new ITabEntry() {

            @Override
            public int getSize() {
                return 0;
            };

            @Override
            public boolean newLine() {
                return true;
            };
        };

        public static class Item implements ITabEntry {

            public final Supplier<ItemStack> stack;

            public Item(Supplier<ItemStack> stack) {
                this.stack = stack;
            };

            @Override
            public void addItems(List<ItemStack> stacks, ItemDisplayParameters parameters, IntConsumer specialRenderLocation) {
                stacks.add(stack.get());
            };

            @Override
            public Collection<ItemStack> getItemsToAddToSearch(ItemDisplayParameters parameters) {
                return Collections.singleton(stack.get());
            };

        };

        public static class ConditionalItem extends Item {

            public final Supplier<Boolean> condition;

            public ConditionalItem(Supplier<ItemStack> stack, Supplier<Boolean> condition) {
                super(stack);
                this.condition = condition;
            };

            @Override
            public void addItems(List<ItemStack> stacks, ItemDisplayParameters parameters, IntConsumer specialRenderLocation) {
                if (condition.get()) super.addItems(stacks, parameters, specialRenderLocation);
            };

            @Override
            public Collection<ItemStack> getItemsToAddToSearch(ItemDisplayParameters parameters) {
                if (!condition.get()) return Collections.emptySet();
                return super.getItemsToAddToSearch(parameters);
            };

        };

        public static class DuplicateItem extends Item {

            public DuplicateItem(Supplier<ItemStack> stack) {
                super(stack);
            };

            @Override
            public Collection<ItemStack> getItemsToAddToSearch(ItemDisplayParameters parameters) {
                return Collections.emptyList();
            };
        };

        public static class Subheading implements ITabEntry {
            private final Component subheading;
            private final GuiTexture backgroundTexture;

            
            public Subheading(Component subheading) {
                this(subheading, GuiTexture.CREATIVE_MODE_TAB_BLANK_ROW);
            }

            
            public Subheading(Component subheading, GuiTexture backgroundTexture) {
                this.subheading = subheading;
                this.backgroundTexture = backgroundTexture;
            }

            @Override
            public int getSize() { return 9; }

            @Override
            public boolean newLine() { return true; }

            @Override
            public boolean hasSpecialRendering() { return true; }

            @Override
            public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
                Minecraft mc = Minecraft.getInstance();
                // removed: this tab drew a gui texture (creative_inventory*) that was never shipped
                graphics.drawString(mc.font, subheading, x + 3, y + 5, 0x5A575A, false);
            }
        }

        public static class Subheading0 implements ITabEntry {
            private final Component subheading;
            private final GuiTexture backgroundTexture;

            
            public Subheading0(Component subheading) {
                this(subheading, GuiTexture.CREATIVE_MODE_TAB_BLANK_ROW_MATERIALS);
            }

            
            public Subheading0(Component subheading, GuiTexture backgroundTexture) {
                this.subheading = subheading;
                this.backgroundTexture = backgroundTexture;
            }

            @Override
            public int getSize() { return 9; }

            @Override
            public boolean newLine() { return true; }

            @Override
            public boolean hasSpecialRendering() { return true; }

            @Override
            public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
                Minecraft mc = Minecraft.getInstance();
                // removed: this tab drew a gui texture (creative_inventory*) that was never shipped
                graphics.drawString(mc.font, subheading, x + 3, y + 5, 0x5A575A, false);
            }
        }

        public static class Subheading1 implements ITabEntry {
            private final Component subheading;
            private final GuiTexture backgroundTexture;

            
            public Subheading1(Component subheading) {
                this(subheading, GuiTexture.CREATIVE_MODE_TAB_BLANK_ROW_SPAWN);
            }

            
            public Subheading1(Component subheading, GuiTexture backgroundTexture) {
                this.subheading = subheading;
                this.backgroundTexture = backgroundTexture;
            }

            @Override
            public int getSize() { return 9; }

            @Override
            public boolean newLine() { return true; }

            @Override
            public boolean hasSpecialRendering() { return true; }

            @Override
            public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
                Minecraft mc = Minecraft.getInstance();
                // removed: this tab drew a gui texture (creative_inventory*) that was never shipped
                graphics.drawString(mc.font, subheading, x + 3, y + 5, 0x5A575A, false);
            }
        }

        public static class Subheading2 implements ITabEntry {
            private final Component subheading;
            private final GuiTexture backgroundTexture;

            
            public Subheading2(Component subheading) {
                this(subheading, GuiTexture.CREATIVE_MODE_TAB_BLANK_ROW_BLOCKS);
            }

            
            public Subheading2(Component subheading, GuiTexture backgroundTexture) {
                this.subheading = subheading;
                this.backgroundTexture = backgroundTexture;
            }

            @Override
            public int getSize() { return 9; }

            @Override
            public boolean newLine() { return true; }

            @Override
            public boolean hasSpecialRendering() { return true; }

            @Override
            public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
                Minecraft mc = Minecraft.getInstance();
                // removed: this tab drew a gui texture (creative_inventory*) that was never shipped
                graphics.drawString(mc.font, subheading, x + 3, y + 5, 0x5A575A, false);
            }
        }
    }
}
