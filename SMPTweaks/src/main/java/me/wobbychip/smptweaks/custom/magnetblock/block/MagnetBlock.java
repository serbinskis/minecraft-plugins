package me.wobbychip.smptweaks.custom.magnetblock.block;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

public class MagnetBlock extends CustomBlock {
    public static double DISTANCE = 30;
    public static double SPEED = 0.1D;

    public MagnetBlock() {
        super("magnet_block", Material.BARREL);
        this.setCustomModel(1000410000);
        this.setCustomName(Main.SYM_COLOR + "rMagnet Block");
        this.setCustomTitle("Magnet Block");
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.shape("ILI", "DBD", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('L', Material.LAPIS_BLOCK);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('B', Material.BARREL);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        return recipe;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public void tick(Block block, long tick) {
        if (!me.wobbychip.smptweaks.custom.magnetblock.MagnetBlock.tweak.getGameRuleBoolean(block.getWorld())) { return; }
        List<Item> items = Utils.getNearbyEntities(block.getLocation(), EntityType.ITEM, DISTANCE, false).stream().map(Item.class::cast).toList();
        boolean pulled = false;

        for (Item item: items) {
            if (Utils.distance(item.getLocation(), block.getLocation().add(0.5, 0.5, 0.5)) > 1.1) { pullItem(item, block, SPEED); continue; }
            if (pulled) { continue; }
            Inventory inventory = ((Container) block.getState()).getInventory();
            if (!addItem(inventory, Utils.cloneItem(item.getItemStack(), 1), true)) { continue; }
            item.setItemStack(Utils.cloneItem(item.getItemStack(), item.getItemStack().getAmount()-1));
            if (item.getItemStack().getAmount() == 0) { item.remove(); }
            pulled = true;
        }
    }

    public static boolean addItem(Inventory inventory, ItemStack itemStack, boolean commit) {
        //Make a copy to restore in case if we run out of space
        ItemStack[] restore = inventory.getContents();

        for (int i = 0; i < restore.length; i++) {
            restore[i] = (restore[i] == null) ? null : restore[i].clone();
        }

        //Add item to destination inventory
        Map<Integer, ItemStack> left = inventory.addItem(itemStack.clone());
        if (!left.isEmpty() || !commit) { inventory.setStorageContents(restore); }
        return left.isEmpty();
    }

    public static void pullItem(Item item, Block block, double speed) {
        Location hLocation = block.getLocation();
        Location tLocation = item.getLocation();

        double f = Utils.distance(hLocation, tLocation);
        double d0 = (hLocation.getX() - tLocation.getX()) / f;
        double d1 = (hLocation.getY() - tLocation.getY()) / f;
        double d2 = (hLocation.getZ() - tLocation.getZ()) / f;
        double dy = Math.max(d1 * d1 * speed, 0.05D);

        Vector vector = item.getVelocity().add(new Vector(Math.copySign(d0 * d0 * speed, d0), Math.copySign(dy, d1), Math.copySign(d2 * d2 * speed, d2)));
        item.setVelocity(vector);
    }
}
