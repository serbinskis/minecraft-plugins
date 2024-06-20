package net.minecraft.world.item;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.IMaterial;

public class CreativeModeTab {

    static final MinecraftKey DEFAULT_BACKGROUND = createTextureLocation("items");
    private final IChatBaseComponent displayName;
    MinecraftKey backgroundTexture;
    boolean canScroll;
    boolean showTitle;
    boolean alignedRight;
    private final CreativeModeTab.f row;
    private final int column;
    private final CreativeModeTab.h type;
    @Nullable
    private ItemStack iconItemStack;
    private Collection<ItemStack> displayItems;
    private Set<ItemStack> displayItemsSearchTab;
    private final Supplier<ItemStack> iconGenerator;
    private final CreativeModeTab.b displayItemsGenerator;

    CreativeModeTab(CreativeModeTab.f creativemodetab_f, int i, CreativeModeTab.h creativemodetab_h, IChatBaseComponent ichatbasecomponent, Supplier<ItemStack> supplier, CreativeModeTab.b creativemodetab_b) {
        this.backgroundTexture = CreativeModeTab.DEFAULT_BACKGROUND;
        this.canScroll = true;
        this.showTitle = true;
        this.alignedRight = false;
        this.displayItems = ItemStackLinkedSet.createTypeAndComponentsSet();
        this.displayItemsSearchTab = ItemStackLinkedSet.createTypeAndComponentsSet();
        this.row = creativemodetab_f;
        this.column = i;
        this.displayName = ichatbasecomponent;
        this.iconGenerator = supplier;
        this.displayItemsGenerator = creativemodetab_b;
        this.type = creativemodetab_h;
    }

    public static MinecraftKey createTextureLocation(String s) {
        return MinecraftKey.withDefaultNamespace("textures/gui/container/creative_inventory/tab_" + s + ".png");
    }

    public static CreativeModeTab.a builder(CreativeModeTab.f creativemodetab_f, int i) {
        return new CreativeModeTab.a(creativemodetab_f, i);
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack == null) {
            this.iconItemStack = (ItemStack) this.iconGenerator.get();
        }

        return this.iconItemStack;
    }

    public MinecraftKey getBackgroundTexture() {
        return this.backgroundTexture;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public int column() {
        return this.column;
    }

    public CreativeModeTab.f row() {
        return this.row;
    }

    public boolean hasAnyItems() {
        return !this.displayItems.isEmpty();
    }

    public boolean shouldDisplay() {
        return this.type != CreativeModeTab.h.CATEGORY || this.hasAnyItems();
    }

    public boolean isAlignedRight() {
        return this.alignedRight;
    }

    public CreativeModeTab.h getType() {
        return this.type;
    }

    public void buildContents(CreativeModeTab.d creativemodetab_d) {
        CreativeModeTab.c creativemodetab_c = new CreativeModeTab.c(this, creativemodetab_d.enabledFeatures);
        ResourceKey resourcekey = (ResourceKey) BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(this).orElseThrow(() -> {
            return new IllegalStateException("Unregistered creative tab: " + String.valueOf(this));
        });

        this.displayItemsGenerator.accept(creativemodetab_d, creativemodetab_c);
        this.displayItems = creativemodetab_c.tabContents;
        this.displayItemsSearchTab = creativemodetab_c.searchTabContents;
    }

    public Collection<ItemStack> getDisplayItems() {
        return this.displayItems;
    }

    public Collection<ItemStack> getSearchTabDisplayItems() {
        return this.displayItemsSearchTab;
    }

    public boolean contains(ItemStack itemstack) {
        return this.displayItemsSearchTab.contains(itemstack);
    }

    public static enum f {

        TOP, BOTTOM;

        private f() {}
    }

    @FunctionalInterface
    public interface b {

        void accept(CreativeModeTab.d creativemodetab_d, CreativeModeTab.e creativemodetab_e);
    }

    public static enum h {

        CATEGORY, INVENTORY, HOTBAR, SEARCH;

        private h() {}
    }

    public static class a {

        private static final CreativeModeTab.b EMPTY_GENERATOR = (creativemodetab_d, creativemodetab_e) -> {
        };
        private final CreativeModeTab.f row;
        private final int column;
        private IChatBaseComponent displayName = IChatBaseComponent.empty();
        private Supplier<ItemStack> iconGenerator = () -> {
            return ItemStack.EMPTY;
        };
        private CreativeModeTab.b displayItemsGenerator;
        private boolean canScroll;
        private boolean showTitle;
        private boolean alignedRight;
        private CreativeModeTab.h type;
        private MinecraftKey backgroundTexture;

        public a(CreativeModeTab.f creativemodetab_f, int i) {
            this.displayItemsGenerator = CreativeModeTab.a.EMPTY_GENERATOR;
            this.canScroll = true;
            this.showTitle = true;
            this.alignedRight = false;
            this.type = CreativeModeTab.h.CATEGORY;
            this.backgroundTexture = CreativeModeTab.DEFAULT_BACKGROUND;
            this.row = creativemodetab_f;
            this.column = i;
        }

        public CreativeModeTab.a title(IChatBaseComponent ichatbasecomponent) {
            this.displayName = ichatbasecomponent;
            return this;
        }

        public CreativeModeTab.a icon(Supplier<ItemStack> supplier) {
            this.iconGenerator = supplier;
            return this;
        }

        public CreativeModeTab.a displayItems(CreativeModeTab.b creativemodetab_b) {
            this.displayItemsGenerator = creativemodetab_b;
            return this;
        }

        public CreativeModeTab.a alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public CreativeModeTab.a hideTitle() {
            this.showTitle = false;
            return this;
        }

        public CreativeModeTab.a noScrollBar() {
            this.canScroll = false;
            return this;
        }

        protected CreativeModeTab.a type(CreativeModeTab.h creativemodetab_h) {
            this.type = creativemodetab_h;
            return this;
        }

        public CreativeModeTab.a backgroundTexture(MinecraftKey minecraftkey) {
            this.backgroundTexture = minecraftkey;
            return this;
        }

        public CreativeModeTab build() {
            if ((this.type == CreativeModeTab.h.HOTBAR || this.type == CreativeModeTab.h.INVENTORY) && this.displayItemsGenerator != CreativeModeTab.a.EMPTY_GENERATOR) {
                throw new IllegalStateException("Special tabs can't have display items");
            } else {
                CreativeModeTab creativemodetab = new CreativeModeTab(this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator);

                creativemodetab.alignedRight = this.alignedRight;
                creativemodetab.showTitle = this.showTitle;
                creativemodetab.canScroll = this.canScroll;
                creativemodetab.backgroundTexture = this.backgroundTexture;
                return creativemodetab;
            }
        }
    }

    private static class c implements CreativeModeTab.e {

        public final Collection<ItemStack> tabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        public final Set<ItemStack> searchTabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        private final CreativeModeTab tab;
        private final FeatureFlagSet featureFlagSet;

        public c(CreativeModeTab creativemodetab, FeatureFlagSet featureflagset) {
            this.tab = creativemodetab;
            this.featureFlagSet = featureflagset;
        }

        @Override
        public void accept(ItemStack itemstack, CreativeModeTab.g creativemodetab_g) {
            if (itemstack.getCount() != 1) {
                throw new IllegalArgumentException("Stack size must be exactly 1");
            } else {
                boolean flag = this.tabContents.contains(itemstack) && creativemodetab_g != CreativeModeTab.g.SEARCH_TAB_ONLY;

                if (flag) {
                    String s = itemstack.getDisplayName().getString();

                    throw new IllegalStateException("Accidentally adding the same item stack twice " + s + " to a Creative Mode Tab: " + this.tab.getDisplayName().getString());
                } else {
                    if (itemstack.getItem().isEnabled(this.featureFlagSet)) {
                        switch (creativemodetab_g.ordinal()) {
                            case 0:
                                this.tabContents.add(itemstack);
                                this.searchTabContents.add(itemstack);
                                break;
                            case 1:
                                this.tabContents.add(itemstack);
                                break;
                            case 2:
                                this.searchTabContents.add(itemstack);
                        }
                    }

                }
            }
        }
    }

    public static record d(FeatureFlagSet enabledFeatures, boolean hasPermissions, HolderLookup.a holders) {

        public boolean needsUpdate(FeatureFlagSet featureflagset, boolean flag, HolderLookup.a holderlookup_a) {
            return !this.enabledFeatures.equals(featureflagset) || this.hasPermissions != flag || this.holders != holderlookup_a;
        }
    }

    public interface e {

        void accept(ItemStack itemstack, CreativeModeTab.g creativemodetab_g);

        default void accept(ItemStack itemstack) {
            this.accept(itemstack, CreativeModeTab.g.PARENT_AND_SEARCH_TABS);
        }

        default void accept(IMaterial imaterial, CreativeModeTab.g creativemodetab_g) {
            this.accept(new ItemStack(imaterial), creativemodetab_g);
        }

        default void accept(IMaterial imaterial) {
            this.accept(new ItemStack(imaterial), CreativeModeTab.g.PARENT_AND_SEARCH_TABS);
        }

        default void acceptAll(Collection<ItemStack> collection, CreativeModeTab.g creativemodetab_g) {
            collection.forEach((itemstack) -> {
                this.accept(itemstack, creativemodetab_g);
            });
        }

        default void acceptAll(Collection<ItemStack> collection) {
            this.acceptAll(collection, CreativeModeTab.g.PARENT_AND_SEARCH_TABS);
        }
    }

    protected static enum g {

        PARENT_AND_SEARCH_TABS, PARENT_TAB_ONLY, SEARCH_TAB_ONLY;

        private g() {}
    }
}
