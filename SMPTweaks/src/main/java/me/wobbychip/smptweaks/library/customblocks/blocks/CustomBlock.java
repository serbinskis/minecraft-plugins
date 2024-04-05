package me.wobbychip.smptweaks.library.customblocks.blocks;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CustomBlock implements Listener {
    public static String TAG_BLOCK = "SMPTWEAKS_CUSTOM_BLOCK";
    public static String TAG_MARKED_ITEM = "SMPTWEAKS_CBLOCK_MARKED";
    private Dispensable dispensable = Dispensable.IGNORE;
    private Comparable comparable = Comparable.IGNORE;
    private ChatColor glow_color = ChatColor.RESET;
    private boolean tickable = false;
    private final Material block_base;
    private final String id;
    private String name;
    private String title;
    private int model = -1;
    private int model_extra = -1;

    public CustomBlock(String id, Material block_base) {
        this.id = id;
        this.block_base = block_base;
    }

    public void setCustomName(String name) {
        this.name = name;
    }

    public void setCustomTitle(String title) {
        this.title = title;
    }

    public void setCustomModel(int model, int model_extra) {
        this.model = model;
        this.model_extra = model_extra;
    }

    public String getId() {
        return id;
    }

    public Material getBlockBase() { return block_base; }

    @Nonnull
    public ItemStack getDropItem(boolean extra) {
        return PersistentUtils.setPersistentDataString(prepareDropItem(extra), TAG_BLOCK, id);
    }

    public void setTickable(boolean tickable) {
        this.tickable = tickable;
    }

    public boolean isTickable() {
        return tickable;
    }

    public void setDispensable(Dispensable dispensable) {
        this.dispensable = dispensable;
    }

    public Dispensable getDispensable() {
        return dispensable;
    }

    public void setComparable(Comparable comparable) {
        this.comparable = comparable;
    }

    public Comparable getComparable() {
        return comparable;
    }

    public void setGlowing(ChatColor glow_color) {
        this.glow_color = glow_color;
    }

    public ChatColor getGlowing() {
        return glow_color;
    }

    public String getCustomTitle() {
        return title;
    }

    @Nullable
    public Recipe getRecipe() {
        return prepareRecipe(new NamespacedKey(Main.plugin, id), getDropItem(false));
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

    public boolean isDirectional() {
        return (block_base.createBlockData() instanceof Directional);
    }

    public void createBlock(ItemDisplay display) {
        display.remove();
        createBlock(display.getLocation().getBlock());
    }

    public void createBlock(Block block) {
        if (block.getType() != block_base) { return; }
        if (isPersistent(block)) { PersistentUtils.setPersistentDataString(block, TAG_BLOCK, id); }
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
        return PersistentUtils.hasPersistentDataString(item, TAG_MARKED_ITEM);
    }

    public ItemStack setMarkedItem(ItemStack item) {
        return PersistentUtils.setPersistentDataString(item, TAG_MARKED_ITEM, id);
    }

    public ItemStack removeMarkedItem(ItemStack item) {
        return PersistentUtils.removePersistentData(item, TAG_MARKED_ITEM);
    }

    public boolean isCustomBlock(ItemStack item) {
        if ((item == null) || (item.getType() != block_base)) { return false; }
        if (!PersistentUtils.hasPersistentDataString(item, TAG_BLOCK)) { return isCustomBlock(item.getItemMeta()); }
        return PersistentUtils.getPersistentDataString(item, TAG_BLOCK).equalsIgnoreCase(id);
    }

    public boolean isCustomBlock(ItemMeta itemMeta) {
        if (!(itemMeta instanceof BlockStateMeta blockMeta)) { return false; }
        if (!(blockMeta.getBlockState() instanceof TileState blockState)) { return false; }
        if (!PersistentUtils.hasPersistentDataString(blockState, TAG_BLOCK)) { return false; }
        return PersistentUtils.getPersistentDataString(blockState, TAG_BLOCK).equalsIgnoreCase(id);
    }

    public boolean isCustomBlock(Block block) {
        CustomMarker marker = CustomMarker.getMarker(block);
        return ((marker != null) && marker.getId().equalsIgnoreCase(id));
    }

    public ItemStack prepareDropItem(boolean extra) {
        ItemStack item = new ItemStack(block_base);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (model > 0) { meta.setCustomModelData(model); }
        if (extra && (model_extra > 0)) { meta.setCustomModelData(model_extra); }
        item.setItemMeta(meta);
        return item;
    }

    public boolean prepareCraft(PrepareItemCraftEvent event, World world, ItemStack result) { return true; }
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) { return null; }
    public int preparePower(Block block) { return -1; }
    public boolean prepareDispense(Block block, HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense) { return false; }
    public void tick(Block block, long tick) {}
    public void remove(Block block) {}

    public enum Dispensable { DISABLE, IGNORE, CUSTOM }
    public enum Comparable { DISABLE, IGNORE, CUSTOM }
}
