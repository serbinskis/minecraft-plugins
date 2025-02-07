package me.serbinskis.smptweaks.library.customblocks.blocks;

import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ServerUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Crafter;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class CustomMarker implements Runnable {
    public static final String TAG_MARKER = "SMPTWEAKS_CUSTOM_MARKER";
    public static final String TAG_DESTROYED = "SMPTWEAKS_CUSTOM_MARKER_DESTROYED";
    private static final HashMap<String, CustomMarker> markers = new HashMap<>();
    private final ItemDisplay display;
    private final CustomBlock customBlock;
    private ChatColor glowing_color;
    private final int task;

    private CustomMarker(ItemDisplay display, CustomBlock customBlock) {
        this.display = display;
        this.customBlock = customBlock;
        this.task = TaskUtils.scheduleSyncRepeatingTask(this, 1L, 1L);
    }

    public String getId() {
        return customBlock.getId();
    }

    public ItemDisplay getDisplay() {
        return display;
    }

    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    public static CustomMarker createMarker(CustomBlock customBlock, Block block) {
        String location = Utils.locationToString(block.getLocation());
        if (markers.containsKey(location)) { markers.get(location).remove(true); }

        Map.Entry<BlockFace, Transformation> orientation = getOrientation(customBlock, block);
        ItemStack itemStack = customBlock.getDropItem(customBlock.prepareTextureIndex(block));

        ItemDisplay display = (ItemDisplay) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.5, 0.5), EntityType.ITEM_DISPLAY);
        PersistentUtils.setPersistentDataString(display, CustomBlock.TAG_BLOCK, customBlock.getId());
        PersistentUtils.setPersistentDataBoolean(display, TAG_MARKER, true);
        display.setBrightness(new Display.Brightness(15, 15));
        display.setCustomName(TAG_MARKER);
        display.setCustomNameVisible(false);
        display.setInvulnerable(true);
        display.setTransformation(orientation.getValue());
        display.setItemStack(itemStack);
        Utils.setGlowColor(display, customBlock.prepareGlowingColor(block));

        CustomMarker marker = new CustomMarker(display, customBlock);
        markers.put(location, marker);
        return marker;
    }

    public void updateTexture(Block block) {
        ItemStack itemStack = customBlock.getDropItem(customBlock.prepareTextureIndex(block));
        display.setItemStack(itemStack);
    }

    public static Map.Entry<BlockFace, Transformation> getOrientation(CustomBlock customBlock, Block block) {
        BlockFace facing = customBlock.isDirectional() ? getFacing(block) : BlockFace.SOUTH;
        Quaternionf left_rotation = new Quaternionf(0f, 0f, 0f, 1f);
        Quaternionf right_rotation = new Quaternionf(0f, 0f, 0f, 1f);

        if (facing.equals(BlockFace.NORTH)) { right_rotation = new Quaternionf(0f, 1f, 0f, 0f); }
        if (facing.equals(BlockFace.EAST)) { left_rotation = new Quaternionf(0f, 0.70710677f, 0f, 0.70710677f); }
        if (facing.equals(BlockFace.WEST)) { left_rotation = new Quaternionf(0f, -0.70710677f, 0f, 0.70710677f); }
        if (facing.equals(BlockFace.UP)) { left_rotation = new Quaternionf(0.70710677f, 0f, 0f, -0.70710677f); }
        if (facing.equals(BlockFace.DOWN)) { left_rotation = new Quaternionf(0.70710677f, 0f, 0f, 0.70710677f); }

        return Map.entry(facing, new Transformation(new Vector3f(0f), left_rotation, new Vector3f(1.002f), right_rotation));
    }

    public static BlockFace getFacing(Block block) {
        if (block.getBlockData() instanceof Directional directional) { return directional.getFacing(); }

        if (block.getBlockData() instanceof Crafter crafter) {
            if (List.of(Crafter.Orientation.NORTH_UP).contains(crafter.getOrientation())) { return BlockFace.NORTH;  }
            if (List.of(Crafter.Orientation.EAST_UP).contains(crafter.getOrientation())) { return BlockFace.EAST; }
            if (List.of(Crafter.Orientation.WEST_UP).contains(crafter.getOrientation())) { return BlockFace.WEST; }
            if (List.of(Crafter.Orientation.UP_EAST, Crafter.Orientation.UP_NORTH, Crafter.Orientation.UP_SOUTH, Crafter.Orientation.UP_WEST).contains(crafter.getOrientation())) { return BlockFace.UP; }
            if (List.of(Crafter.Orientation.DOWN_EAST, Crafter.Orientation.DOWN_NORTH, Crafter.Orientation.DOWN_SOUTH, Crafter.Orientation.DOWN_WEST).contains(crafter.getOrientation())) { return BlockFace.DOWN; }
        }

        return BlockFace.SOUTH;
    }

    public void setGlowing(ChatColor color) {
        if (glowing_color == color) { return; }
        Utils.setGlowColor(display, color);
        glowing_color = color;
    }

    public static boolean isMarkerEntity(Entity entity) {
        if (!(entity instanceof ItemDisplay)) { return false; }
        return PersistentUtils.hasPersistentDataBoolean(entity, TAG_MARKER);
    }

    public static boolean containsMarkerEntity(Entity entity) {
        return markers.containsKey(Utils.locationToString(entity.getLocation().getBlock().getLocation()));
    }

    public static CustomMarker getMarker(Block block) {
        return markers.get(Utils.locationToString(block.getLocation()));
    }

    @SuppressWarnings({"unchecked"})
    public static void collectUnmarkedBlocks(Object... data) {
        ArrayList<Entity> entities = new ArrayList<>();

        if (data.length == 0) { Bukkit.getWorlds().forEach(e -> entities.addAll(e.getEntities())); }
        if ((data.length > 0) && (data[0] instanceof Chunk chunk)) { entities.addAll(List.of(chunk.getEntities())); }
        if ((data.length > 0) && (data[0] instanceof List<?> list)) { entities.addAll((List<Entity>) list); }

        entities.stream().filter(e -> isMarkerEntity(e) && !containsMarkerEntity(e)).forEach(entity -> {
            CustomBlock customBlock = CustomBlocks.getCustomBlock((ItemDisplay) entity);
            if (customBlock != null) { customBlock.createBlock((ItemDisplay) entity); }
        });
    }

    public void setDestroyed(boolean destroyed) {
        PersistentUtils.setPersistentDataBoolean(display, TAG_DESTROYED, destroyed);
    }

    public boolean isDestroyed() {
        if (!PersistentUtils.hasPersistentDataBoolean(display, TAG_DESTROYED)) { return false; }
        return PersistentUtils.getPersistentDataBoolean(display, TAG_DESTROYED);
    }

    public void recreate() {
        remove(true);
        Block block = display.getLocation().getBlock();
        if (block.getType() != customBlock.getBlockBase()) { return; }
        CustomMarker.createMarker(customBlock, block);
    }

    public void remove(boolean rmarker) {
        //markers.values().removeIf(e -> e.display.getUniqueId().equals(display.getUniqueId()));
        markers.remove(Utils.locationToString(display.getLocation().getBlock().getLocation()));
        TaskUtils.cancelTask(task);
        if (rmarker) { display.remove(); }
    }

    public void run() {
        if (ServerUtils.isPaused()) { return; }
        Block block = display.getLocation().getBlock();
        Location location = display.getLocation();
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) { remove(false); return; }
        if (!display.isValid()) { recreate(); return; }

        if (block.getType() != customBlock.getBlockBase()) {
            boolean destroyed = isDestroyed();
            remove(true);
            if (!destroyed) { customBlock.remove(block, false); }
            return;
        }

        if (customBlock.isTickable()) { customBlock.tick(block, ServerUtils.getTick()); }
    }
}
