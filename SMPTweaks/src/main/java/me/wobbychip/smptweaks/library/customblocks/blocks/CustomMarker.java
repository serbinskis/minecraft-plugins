package me.wobbychip.smptweaks.library.customblocks.blocks;

import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.HashMap;

public class CustomMarker implements Runnable {
    public static String MARKER_TAG = "SMPTWEAKS_CUSTOM_MARKER";
    public static final HashMap<String, CustomMarker> markers = new HashMap<>();
    public final int task;
    public final BlockDisplay display;
    public final CustomBlock cblock;

    private CustomMarker(BlockDisplay display, CustomBlock cblock) {
        this.display = display;
        this.cblock = cblock;
        this.task = TaskUtils.scheduleSyncRepeatingTask(this, 1L, 1L);
    }

    public static CustomMarker createMarker(CustomBlock cblock, Block block) {
        String location = Utils.locationToString(block.getLocation());
        if (markers.containsKey(location)) { return markers.get(location); }

        BlockDisplay display = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.5, 0.5), EntityType.BLOCK_DISPLAY);
        PersistentUtils.setPersistentDataString(display, CustomBlock.BLOCK_TAG, cblock.getName());
        PersistentUtils.setPersistentDataBoolean(display, MARKER_TAG, true);
        display.setCustomName(MARKER_TAG);
        display.setCustomNameVisible(false);
        display.setInvulnerable(true);
        display.setBlock(getBlockData(block, cblock.getCustomMaterial()));

        Transformation tranf = display.getTransformation();
        display.setTransformation(new Transformation(new Vector3f(-0.5001f), tranf.getLeftRotation(), new Vector3f(1.0002f), tranf.getRightRotation()));

        CustomMarker marker = new CustomMarker(display, cblock);
        markers.put(location, marker);

        return marker;
    }

    public static BlockData getBlockData(Block block, Material material) {
        BlockData blockData = material.createBlockData();
        if (!(blockData instanceof Directional)) { return blockData; }
        if (!(block.getBlockData() instanceof Directional dblock)) { return blockData; }
        ((Directional) blockData).setFacing(dblock.getFacing());
        return blockData;
    }

    public static boolean isMarkerEntity(Entity entity) {
        if (!(entity instanceof BlockDisplay)) { return false; }
        return PersistentUtils.hasPersistentDataBoolean(entity, MARKER_TAG);
    }

    public static boolean containsMarkerEntity(Entity entity) {
        String location = Utils.locationToString(entity.getLocation().getBlock().getLocation());
        return markers.containsKey(location);
    }

    public static CustomMarker getMarker(Block block) {
        for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), 0.4, 0.4, 0.4)) {
            if (!isMarkerEntity(entity)) { continue; }
            String lcoation = Utils.locationToString(entity.getLocation().getBlock().getLocation());
            if (markers.containsKey(lcoation)) { return markers.get(lcoation); }
        }

        return null;
    }

    //This is used to collect blocks and mark them again after server reload
    //This is very inefficient because it runs every tick, to check if new markers did load
    public static HashMap<Block, BlockDisplay> collectUnmarkedBlocks() {
        HashMap<Block, BlockDisplay> blocks = new HashMap<>();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!isMarkerEntity(entity)) { continue; }
                if (containsMarkerEntity(entity)) { continue; }
                blocks.put(entity.getLocation().getBlock(), (BlockDisplay) entity);
            }
        }

        return blocks;
    }

    public CustomBlock getCustomBlock() {
        return cblock;
    }

    public String getName() {
        return cblock.getName();
    }

    private void recreate() {
        remove(true);
        Block block = display.getLocation().getBlock();
        if (block.getType() != cblock.getBlockBase()) { return; }
        CustomMarker.createMarker(cblock, block);
    }

    public void run() {
        if (ServerUtils.isPaused()) { return; }
        Block block = display.getLocation().getBlock();
        Location location = display.getLocation();
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) { return; }
        if (!display.isValid()) { recreate(); return; }
        if (block.getType() != cblock.getBlockBase()) { remove(true); return; }
        if (cblock.isTickable()) { cblock.tick(block, ServerUtils.getTick()); }
    }

    public void remove(boolean rmarker) {
        markers.remove(Utils.locationToString(display.getLocation().getBlock().getLocation()));
        TaskUtils.cancelSyncRepeatingTask(task);
        if (rmarker) { display.remove(); }
    }
}
