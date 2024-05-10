package wobbychip.main.spawnproofer;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.text.Text;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.*;

import static net.minecraft.block.FlowerbedBlock.FLOWER_AMOUNT;
import static net.minecraft.item.Item.BLOCK_ITEMS;

public class SpawnProoferHelper {
    private final static MinecraftClient minecraft = MinecraftClient.getInstance();
    private static boolean enabled = false;
    private static String previousMessage = null;
    private static final LinkedHashMap<Long, Long> nanotimeMap = new LinkedHashMap<>();
    private static final List<TagKey<Item>> spawnProofItems = List.of(ItemTags.WOOL_CARPETS, ItemTags.SLABS, ItemTags.WOODEN_PRESSURE_PLATES, ItemTags.BUTTONS);
	private static final List<Item> extraItemList = List.of(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.TORCH, Items.PINK_PETALS, Items.BONE_MEAL);

    public static void tick() {
        if (enabled) { doSpawnProofing(); }
    }

    public static void toggle() {
        enabled = !enabled;
        sendActionMessage("[SpawnProofer] Turned " + (enabled ? "ON" : "OFF"));
    }

    public static void changeDistance(int i) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        config.reachDistance += i;
        AutoConfig.getConfigHolder(ModConfig.class).save();
        sendActionMessage("[SpawnProofer] Reach Distance: " + config.reachDistance);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void sendActionMessage(String message) {
        if ((previousMessage != null) && previousMessage.equals(message)) { return; }
        previousMessage = message;
        MinecraftClient.getInstance().player.sendMessage(Text.of(message), true);
    }

    public static void sendChatMessage(String message) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(message));
    }

    public static boolean isSpawnableBlock(BlockPos blockPos, Item item, boolean light, int minLighting) {
        World world = MinecraftClient.getInstance().world;
        if (world == null) { return false; }

        Block block = world.getBlockState(blockPos).getBlock();
        if (Items.BONE_MEAL.equals(item) && (block instanceof Fertilizable)) { return true; }
        if (block instanceof FluidBlock) { return false; }

        if (!world.getBlockState(blockPos.down()).allowsSpawning(world, blockPos.down(), EntityType.ZOMBIFIED_PIGLIN)) { return false; }
        boolean arg0 = world.isAir(blockPos) || ((world.getBlockState(blockPos).getHardness(world, blockPos) == 0.0f) || world.getBlockState(blockPos).isReplaceable());
        if (light) { arg0 = arg0 && (world.getLightLevel(LightType.BLOCK, blockPos) < minLighting); }
        return arg0;
    }

    public static boolean isItemBlock(BlockPos blockPos, Item item) {
        if (item == null) { return false; } //Let it pass, so we will se the message in placeBlock()
        World world = MinecraftClient.getInstance().world;
        if (world == null) { return false; }

        Block block = world.getBlockState(blockPos).getBlock();
        if (Items.PINK_PETALS.equals(item)) { return false; }
        if (Items.BONE_MEAL.equals(item) && (block instanceof Fertilizable)) { return false; } //This is inverted in filter
        Item blockItem = BLOCK_ITEMS.getOrDefault(block, Items.AIR);
        return blockItem.equals(item);
    }

    public static boolean isCombinable(BlockPos blockPos, Item item) {
        World world = MinecraftClient.getInstance().world;
        if (world == null) { return false; }
        Block block = world.getBlockState(blockPos).getBlock();

        if (Items.BONE_MEAL.equals(item)) {
            if (!(block instanceof Fertilizable) || (block instanceof GrassBlock)) { return false; } //Skip non flower blocks
            if (!(block instanceof FlowerbedBlock)) { return true; } //Skip non flower blocks
            return world.getBlockState(blockPos).get(FLOWER_AMOUNT) < 4;
        }

        if (Items.PINK_PETALS.equals(item)) {
            if (!BLOCK_ITEMS.getOrDefault(block, Items.AIR).equals(item)) { return true; } //Skip if not using petals on petals
            return world.getBlockState(blockPos).get(FLOWER_AMOUNT) < 4;
        }

        return true;
    }

    public static boolean isInstantBreakable(BlockPos blockPos, Item item) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        World world = MinecraftClient.getInstance().world;
        if (world == null) { return false; }

        Block block = world.getBlockState(blockPos).getBlock();
        if (Items.BONE_MEAL.equals(item) && (block instanceof Fertilizable)) { return true; }
        if (BLOCK_ITEMS.getOrDefault(block, Items.AIR).equals(Items.AIR)) { return true; }
        if (Items.PINK_PETALS.equals(item) && block.equals(Blocks.PINK_PETALS)) { return true; }
        if (Items.TORCH.equals(item) && block.equals(Blocks.TORCH)) { return false; }
        if ((world.getBlockState(blockPos).getHardness(world, blockPos) <= 0.0f) && !world.getBlockState(blockPos).isReplaceable()) { return config.replace; }
        return true;
    }

    public static void doSpawnProofing() {
        Object[] result = getItem();
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        int reachDistance = config.reachDistance;
        int maxInteractionPerTick = Items.TORCH.equals(result[1]) ? 1 : config.maxInteractionPerTick;
        int minLighting = Items.TORCH.equals(result[1]) ? config.minLighting : Integer.MAX_VALUE;
        boolean light = Items.TORCH.equals(result[1]);

        for (int  i = 0; i < maxInteractionPerTick; i++) {
            BlockPos.streamOutwards(playerPos, reachDistance, reachDistance, reachDistance).
                    filter(e -> isSpawnableBlock(e, (Item) result[1], light, minLighting)).
                    filter(e -> isInstantBreakable(e, (Item) result[1])).
                    filter(e -> isCombinable(e, (Item) result[1])).
                    filter(e -> !isItemBlock(e, (Item) result[1])).
                    filter(e -> playerPos.getSquaredDistance(e) < reachDistance * reachDistance).
                    filter(e -> !nanotimeMap.containsValue(e.asLong()) || (nanotimeMap.get(e.asLong()) > System.nanoTime()+1e9)).
                    limit(1).forEach(e -> placeBlock(e, (Integer) result[0], (Item) result[1]));
        }
    }

    private static void placeBlock(BlockPos blockPos, int slotNum, Item item) {
        World world = MinecraftClient.getInstance().world;
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        if (slotNum == -1) {
            sendActionMessage("[SpawnProofer] No item in the inventory");
            return;
        }

        if (!playerInventorySwitch(minecraft.player.getInventory().getStack(slotNum).getItem())) {
            sendActionMessage("[SpawnProofer] Failed switching items in the inventory");
            return;
        }

        if (config.replace && !Items.BONE_MEAL.equals(item) && (world.getBlockState(blockPos).getHardness(world, blockPos) <= 0.0f) && !world.getBlockState(blockPos).isReplaceable()) {
            minecraft.interactionManager.attackBlock(blockPos, Direction.NORTH);
            minecraft.interactionManager.breakBlock(blockPos);
        }

        Vec3d hitVec = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.NORTH, blockPos, false);
        minecraft.interactionManager.interactBlock(minecraft.player, Hand.MAIN_HAND, hitResult);
        nanotimeMap.put(blockPos.asLong(), System.nanoTime());
    }

    private static Object[] getItem() {
	    Inventory inventory = minecraft.player.getInventory();

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);

			for (TagKey<Item> predicates : spawnProofItems) {
				if (stack.isIn(predicates)) { return new Object[] { i, stack.getItem() }; }
			}

            if (extraItemList.contains(stack.getItem())) { return new Object[] { i, stack.getItem() }; }
		}

        return new Object[] { -1, null };
    }

    public static boolean playerInventorySwitch(Item itemName) {
        ItemStack itemStack = itemName.getDefaultStack();
        PlayerInventory playerInventory = minecraft.player.getInventory();
        int i = playerInventory.getSlotWithStack(itemStack);
        if (i == -1) { return false; }

        if (PlayerInventory.isValidHotbarIndex(i)) {
            playerInventory.selectedSlot = i;
        } else {
            minecraft.interactionManager.pickFromInventory(i);
        }

        minecraft.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(playerInventory.selectedSlot));
        return true;
    }
}