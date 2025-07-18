package me.serbinskis.smptweaks.library.customblocks.blocks;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customtextures.TextureSplitter;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Crafter;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomBlock implements Listener {
    public static String TAG_BLOCK = "SMPTWEAKS_CUSTOM_BLOCK";
    public static String TAG_MARKED_ITEM = "SMPTWEAKS_CBLOCK_MARKED";
    private Dispensable dispensable = Dispensable.IGNORE;
    private Comparable comparable = Comparable.IGNORE;
    private ChatColor glow_color = ChatColor.RESET;
    private boolean tickable = true;
    private final Material block_base;
    private final String id;
    private String name;
    private String title;
    private String texture;

    public CustomBlock(String id, Material block_base) {
        this.id = id;
        this.name = id;
        this.block_base = block_base;
    }

    public String getId() {
        return id;
    }

    public void setCustomName(String name) {
        this.name = name;
    }

    public String getCustomName() {
        return name;
    }

    public void setCustomTitle(String title) {
        this.title = title;
    }

    public String getCustomTitle() {
        return title;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public byte[][] getCustomTextures() {
        if (texture == null) { return null; }
        InputStream inputStream = Main.plugin.getResource("textures/blocks/" + texture);
        if (inputStream == null) { return null; }
        return TextureSplitter.splitTexture(inputStream);
    }

    public Material getBlockBase() { return block_base; }

    @Nonnull
    public ItemStack getDropItem(int textureIndex) {
        return PersistentUtils.setPersistentDataString(prepareDropItem(textureIndex), TAG_BLOCK, id);
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

    public void setGlowing(Block block, ChatColor glow_color) {
        CustomMarker marker = getMarker(block);
        if (marker != null) { marker.setGlowing(glow_color); }
    }

    public ChatColor getGlowing() {
        return glow_color;
    }

    public int prepareTextureIndex(Block block) {
        Map.Entry<BlockFace, Transformation> orientation = CustomMarker.getOrientation(this, block);
        if (Arrays.asList(BlockFace.UP, BlockFace.DOWN).contains(orientation.getKey())) { return 1; }
        return 0;
    }

    @Nullable
    public Recipe getRecipe() {
        return prepareRecipe(new NamespacedKey(Main.plugin, id), getDropItem(0));
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
        BlockData blockData = block_base.createBlockData();
        return ((blockData instanceof Directional) || (blockData instanceof Crafter));
    }

    public boolean isPowered(Block block) { return (block.isBlockIndirectlyPowered() || block.isBlockPowered()); }

    public void createBlock(ItemDisplay display) {
        display.remove();
        Block block = display.getLocation().getBlock();
        if (block.getType() != block_base) { return; }
        createBlock(block, false);
    }

    public void createBlock(Block block, boolean new_block) {
        CustomMarker.createMarker(this, block);

        if (hasInventory() && (title != null)) {
            Container container = ((Container) block.getState());
            container.setCustomName(title);
            container.update();
        }

        this.create(block, new_block);
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

    public static CustomMarker getMarker(Block block) {
        return CustomMarker.getMarker(block);
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

    public ItemStack prepareDropItem(int textureIndex) {
        ItemStack item = new ItemStack(block_base);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (this.texture != null) {
            meta.setItemModel(new NamespacedKey("smptweaks", "blocks/" + this.id + "_" + textureIndex));
        }

        item.setItemMeta(meta);
        return item;
    }

    @Nullable
    public ItemStack prepareCraft(@Nullable PrepareItemCraftEvent event, World world, ItemStack result) { return result; }
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) { return null; }
    public int preparePower(Block block) { return -1; }
    public boolean prepareDispense(Block block, HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense) { return false; }
    public boolean prepareInventory(InventoryOpenEvent event, Block block) { return true; }
    public ChatColor prepareGlowingColor(Block block) { return this.glow_color; }
    public void tick(Block block, long tick) {}
    public void create(Block block, boolean new_block) {}
    public void remove(Block block, boolean intentional) {}

    public enum Dispensable { DISABLE, IGNORE, CUSTOM }
    public enum Comparable { DISABLE, IGNORE, CUSTOM }
}
