package me.wobbychip.smptweaks.library.customblocks.blocks;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomBlock implements Listener {
    public static String BLOCK_TAG = "SMPTWEAKS_CUSTOM_BLOCK";
    public static String MARKED_ITEM = "SMPTWEAKS_CBLOCK_MARKED";
    public final String name;
    public final Material block_base;
    public String title;
    public boolean tickable = false;
    public Material custom_material = Material.AIR;

    public CustomBlock(String name, Material block_base) {
        this.name = name;
        this.block_base = block_base;
    }

    public String getName() {
        return name;
    }

    public Material getBlockBase() {
        return block_base;
    }

    @Nonnull
    public ItemStack getDropItem() {
        return PersistentUtils.setPersistentDataString(prepareDropItem(), BLOCK_TAG, name);
    }

    public void setTickable(boolean tickable) {
        this.tickable = tickable;
    }

    public boolean isTickable() {
        return tickable;
    }

    protected void setCustomTitle(String title) {
        this.title = title;
    }

    public String getCustomTitle() {
        return title;
    }

    protected void setCustomMaterial(Material custom_material) {
        this.custom_material = custom_material;
    }

    protected Material getCustomMaterial() {
        return custom_material;
    }

    @Nullable
    public Recipe getRecipe() {
        return prepareRecipe(new NamespacedKey((Plugin) Main.plugin, name));
    }

    public boolean hasInventory() {
        return (block_base.createBlockData().createBlockState() instanceof Container);
    }

    public boolean isPersistent(Block block) {
        return (block.getState() instanceof TileState);
    }

    public boolean isPowerable() {
        return (block_base.createBlockData() instanceof Powerable);
    }

    public boolean isAnaloguePowerable() {
        return (block_base.createBlockData() instanceof AnaloguePowerable);
    }

    public void createBlock(Block block) {
        if (block.getType() != block_base) { return; }
        if (isPersistent(block)) { PersistentUtils.setPersistentDataString(block, BLOCK_TAG, name); }
        CustomMarker.createMarker(this, block);

        if (hasInventory() && (title != null)) {
            Container container = ((Container) block.getState());
            container.setCustomName(title);
            container.update();
        }
    }

    public void removeBlock(Block block) {
        ReflectionUtils.forceUpdateNeighbors(block, 1, Material.COMPARATOR, null);
        CustomMarker customMarker = CustomMarker.getMarker(block);
        if (customMarker != null) { customMarker.remove(true); }
    }

    public void setMarkedInventory(Block block) {
        if (!hasInventory()) { return; }
        Container container = (Container) block.getState();
        Inventory inv = container.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack itemStack = inv.getItem(i);
            if ((itemStack == null) || isCustomBlock(itemStack)) { continue; } //Case, where custom block was inside inventory (skip it)
            if (itemStack.getType() != block_base) { continue; } //We only need to difference item type of cblock
            inv.setItem(i, setMarkedItem(itemStack)); //Add tag to difference normal item from custom block inside BlockDropItemEvent
        }
    }

    public boolean isMarkedItem(ItemStack item) {
        return PersistentUtils.hasPersistentDataString(item, MARKED_ITEM);
    }

    public ItemStack setMarkedItem(ItemStack item) {
        return PersistentUtils.setPersistentDataString(item, MARKED_ITEM, name);
    }

    public ItemStack removeMarkedItem(ItemStack item) {
        return PersistentUtils.removePersistentData(item, MARKED_ITEM);
    }

    public boolean isCustomBlock(ItemStack item) {
        if ((item == null) || (item.getType() != block_base)) { return false; }
        if (!PersistentUtils.hasPersistentDataString(item, BLOCK_TAG)) { return isCustomBlock(item.getItemMeta()); }
        return PersistentUtils.getPersistentDataString(item, BLOCK_TAG).equalsIgnoreCase(name);
    }

    public boolean isCustomBlock(ItemMeta itemMeta) {
        if (!(itemMeta instanceof BlockStateMeta blockMeta)) { return false; }
        if (!(blockMeta.getBlockState() instanceof TileState blockState)) { return false; }
        if (!PersistentUtils.hasPersistentDataString(blockState, BLOCK_TAG)) { return false; }
        return PersistentUtils.getPersistentDataString(blockState, BLOCK_TAG).equalsIgnoreCase(name);
    }

    public boolean isCustomBlock(Block block) {
        CustomMarker marker = CustomMarker.getMarker(block);
        return ((marker != null) && marker.getName().equalsIgnoreCase(name));
    }

    public boolean isCustomBlock(BlockState state) {
        if ((state.getType() != block_base) || !(state instanceof TileState) ){ return false; }
        if (!PersistentUtils.hasPersistentDataString(state, BLOCK_TAG)) { return false; }
        return PersistentUtils.getPersistentDataString(state, BLOCK_TAG).equalsIgnoreCase(name);
    }

    public boolean prepareCraft(PrepareItemCraftEvent event, World world, ItemStack result) { return true; }
    public ItemStack prepareDropItem() { return new ItemStack(Material.AIR); }
    public Recipe prepareRecipe(NamespacedKey key) { return null; }
    public int preparePower(Block block) { return -1; }
    public void tick(Block block, long tick) {}
}
