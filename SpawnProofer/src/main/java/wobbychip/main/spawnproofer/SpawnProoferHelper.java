package wobbychip.main.spawnproofer;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
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
import net.minecraft.world.World;

import java.util.*;

public class SpawnProoferHelper {
    private final static MinecraftClient minecraft = MinecraftClient.getInstance();
    private static boolean enabled = false;
    private static String previousMessage = null;
    private static final LinkedHashMap<Long, Long> nanotimeMap = new LinkedHashMap<>();
    private static final List<TagKey<Item>> spawnProofItems = List.of(ItemTags.WOOL_CARPETS, ItemTags.SLABS, ItemTags.WOODEN_PRESSURE_PLATES, ItemTags.BUTTONS);
	private static final List<Item> extraItemList = List.of(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.HEAVY_WEIGHTED_PRESSURE_PLATE);

    public static void tick() {
        if (enabled) { doSpawnProofing(); }
    }

    public static void toggle() {
        enabled = !enabled;
        sendActionMessage("[SpawnProofer] Turned " + (enabled ? "ON" : "OFF"));
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void sendActionMessage(String message) {
        if ((previousMessage != null) && previousMessage.equals(message)) { return; }
        previousMessage = message;
        MinecraftClient.getInstance().player.sendMessage(Text.of(message), true);
    }

    public static boolean isSpawnableBlock(long longPos) {
        World world = MinecraftClient.getInstance().world;
        if (world == null) { return false; }

        BlockPos blockPos = BlockPos.fromLong(longPos);
        Block block = world.getBlockState(blockPos).getBlock();
        if (block instanceof FluidBlock) { return false; }

        if (!world.getBlockState(blockPos.down()).allowsSpawning(world, blockPos.down(), EntityType.ZOMBIFIED_PIGLIN)) { return false; }
        return world.isAir(blockPos) || ((world.getBlockState(blockPos).getHardness(world, blockPos) == 0.0f) || world.getBlockState(blockPos).isReplaceable());
    }

    public static void doSpawnProofing() {
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        int reachDistance = config.reachDistance;

        for (int  i = 0; i < config.maxInteractionPerTick; i++) {
            BlockPos.streamOutwards(playerPos, reachDistance, reachDistance, reachDistance).
                    filter(e -> isSpawnableBlock(e.asLong())).
                    filter(e -> playerPos.getSquaredDistance(e)<reachDistance * reachDistance).
                    filter(e -> !nanotimeMap.containsValue(e.asLong()) || (nanotimeMap.get(e.asLong()) > System.nanoTime()+1e9)).
                    limit(1).forEach(SpawnProoferHelper::placeBlock);
        }
    }

    private static void placeBlock(BlockPos blockPos) {
        int slotNum = getCarpetItem();
        World world = MinecraftClient.getInstance().world;

        if (slotNum == -1) {
            sendActionMessage("[SpawnProofer] No blocks in the inventory");
            return;
        }

        if (!playerInventorySwitch(minecraft.player.getInventory().getStack(slotNum).getItem())) {
            sendActionMessage("[SpawnProofer] Failed switching items in the inventory");
            return;
        }

        if ((world.getBlockState(blockPos).getHardness(world, blockPos) <= 0.0f) && !world.getBlockState(blockPos).isReplaceable()) {
            minecraft.interactionManager.attackBlock(blockPos, Direction.NORTH);
            minecraft.interactionManager.breakBlock(blockPos);
        }

        Vec3d hitVec = new Vec3d(blockPos.getX() , blockPos.getY(), blockPos.getZ());
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.NORTH, blockPos, false);
        minecraft.interactionManager.interactBlock(minecraft.player, Hand.MAIN_HAND, hitResult);
        nanotimeMap.put(blockPos.asLong(),System.nanoTime());
    }
    private static int getCarpetItem(){
	    Inventory inventory = minecraft.player.getInventory();

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);

			for (TagKey<Item> predicates : spawnProofItems){
				if (stack.isIn(predicates)) { return i; }
			}

            if (extraItemList.contains(stack.getItem()))  { return i; }
		}

        return -1;
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