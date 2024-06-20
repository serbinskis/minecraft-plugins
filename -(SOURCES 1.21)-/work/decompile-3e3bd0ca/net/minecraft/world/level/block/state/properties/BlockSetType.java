package net.minecraft.world.level.block.state.properties;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.level.block.SoundEffectType;

public record BlockSetType(String name, boolean canOpenByHand, boolean canOpenByWindCharge, boolean canButtonBeActivatedByArrows, BlockSetType.a pressurePlateSensitivity, SoundEffectType soundType, SoundEffect doorClose, SoundEffect doorOpen, SoundEffect trapdoorClose, SoundEffect trapdoorOpen, SoundEffect pressurePlateClickOff, SoundEffect pressurePlateClickOn, SoundEffect buttonClickOff, SoundEffect buttonClickOn) {

    private static final Map<String, BlockSetType> TYPES = new Object2ObjectArrayMap();
    public static final Codec<BlockSetType> CODEC;
    public static final BlockSetType IRON;
    public static final BlockSetType COPPER;
    public static final BlockSetType GOLD;
    public static final BlockSetType STONE;
    public static final BlockSetType POLISHED_BLACKSTONE;
    public static final BlockSetType OAK;
    public static final BlockSetType SPRUCE;
    public static final BlockSetType BIRCH;
    public static final BlockSetType ACACIA;
    public static final BlockSetType CHERRY;
    public static final BlockSetType JUNGLE;
    public static final BlockSetType DARK_OAK;
    public static final BlockSetType CRIMSON;
    public static final BlockSetType WARPED;
    public static final BlockSetType MANGROVE;
    public static final BlockSetType BAMBOO;

    public BlockSetType(String s) {
        this(s, true, true, true, BlockSetType.a.EVERYTHING, SoundEffectType.WOOD, SoundEffects.WOODEN_DOOR_CLOSE, SoundEffects.WOODEN_DOOR_OPEN, SoundEffects.WOODEN_TRAPDOOR_CLOSE, SoundEffects.WOODEN_TRAPDOOR_OPEN, SoundEffects.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundEffects.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundEffects.WOODEN_BUTTON_CLICK_OFF, SoundEffects.WOODEN_BUTTON_CLICK_ON);
    }

    private static BlockSetType register(BlockSetType blocksettype) {
        BlockSetType.TYPES.put(blocksettype.name, blocksettype);
        return blocksettype;
    }

    public static Stream<BlockSetType> values() {
        return BlockSetType.TYPES.values().stream();
    }

    static {
        Function function = BlockSetType::name;
        Map map = BlockSetType.TYPES;

        Objects.requireNonNull(map);
        CODEC = Codec.stringResolver(function, map::get);
        IRON = register(new BlockSetType("iron", false, false, false, BlockSetType.a.EVERYTHING, SoundEffectType.METAL, SoundEffects.IRON_DOOR_CLOSE, SoundEffects.IRON_DOOR_OPEN, SoundEffects.IRON_TRAPDOOR_CLOSE, SoundEffects.IRON_TRAPDOOR_OPEN, SoundEffects.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEffects.METAL_PRESSURE_PLATE_CLICK_ON, SoundEffects.STONE_BUTTON_CLICK_OFF, SoundEffects.STONE_BUTTON_CLICK_ON));
        COPPER = register(new BlockSetType("copper", true, true, false, BlockSetType.a.EVERYTHING, SoundEffectType.COPPER, SoundEffects.COPPER_DOOR_CLOSE, SoundEffects.COPPER_DOOR_OPEN, SoundEffects.COPPER_TRAPDOOR_CLOSE, SoundEffects.COPPER_TRAPDOOR_OPEN, SoundEffects.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEffects.METAL_PRESSURE_PLATE_CLICK_ON, SoundEffects.STONE_BUTTON_CLICK_OFF, SoundEffects.STONE_BUTTON_CLICK_ON));
        GOLD = register(new BlockSetType("gold", false, true, false, BlockSetType.a.EVERYTHING, SoundEffectType.METAL, SoundEffects.IRON_DOOR_CLOSE, SoundEffects.IRON_DOOR_OPEN, SoundEffects.IRON_TRAPDOOR_CLOSE, SoundEffects.IRON_TRAPDOOR_OPEN, SoundEffects.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEffects.METAL_PRESSURE_PLATE_CLICK_ON, SoundEffects.STONE_BUTTON_CLICK_OFF, SoundEffects.STONE_BUTTON_CLICK_ON));
        STONE = register(new BlockSetType("stone", true, true, false, BlockSetType.a.MOBS, SoundEffectType.STONE, SoundEffects.IRON_DOOR_CLOSE, SoundEffects.IRON_DOOR_OPEN, SoundEffects.IRON_TRAPDOOR_CLOSE, SoundEffects.IRON_TRAPDOOR_OPEN, SoundEffects.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEffects.STONE_PRESSURE_PLATE_CLICK_ON, SoundEffects.STONE_BUTTON_CLICK_OFF, SoundEffects.STONE_BUTTON_CLICK_ON));
        POLISHED_BLACKSTONE = register(new BlockSetType("polished_blackstone", true, true, false, BlockSetType.a.MOBS, SoundEffectType.STONE, SoundEffects.IRON_DOOR_CLOSE, SoundEffects.IRON_DOOR_OPEN, SoundEffects.IRON_TRAPDOOR_CLOSE, SoundEffects.IRON_TRAPDOOR_OPEN, SoundEffects.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEffects.STONE_PRESSURE_PLATE_CLICK_ON, SoundEffects.STONE_BUTTON_CLICK_OFF, SoundEffects.STONE_BUTTON_CLICK_ON));
        OAK = register(new BlockSetType("oak"));
        SPRUCE = register(new BlockSetType("spruce"));
        BIRCH = register(new BlockSetType("birch"));
        ACACIA = register(new BlockSetType("acacia"));
        CHERRY = register(new BlockSetType("cherry", true, true, true, BlockSetType.a.EVERYTHING, SoundEffectType.CHERRY_WOOD, SoundEffects.CHERRY_WOOD_DOOR_CLOSE, SoundEffects.CHERRY_WOOD_DOOR_OPEN, SoundEffects.CHERRY_WOOD_TRAPDOOR_CLOSE, SoundEffects.CHERRY_WOOD_TRAPDOOR_OPEN, SoundEffects.CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEffects.CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEffects.CHERRY_WOOD_BUTTON_CLICK_OFF, SoundEffects.CHERRY_WOOD_BUTTON_CLICK_ON));
        JUNGLE = register(new BlockSetType("jungle"));
        DARK_OAK = register(new BlockSetType("dark_oak"));
        CRIMSON = register(new BlockSetType("crimson", true, true, true, BlockSetType.a.EVERYTHING, SoundEffectType.NETHER_WOOD, SoundEffects.NETHER_WOOD_DOOR_CLOSE, SoundEffects.NETHER_WOOD_DOOR_OPEN, SoundEffects.NETHER_WOOD_TRAPDOOR_CLOSE, SoundEffects.NETHER_WOOD_TRAPDOOR_OPEN, SoundEffects.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEffects.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEffects.NETHER_WOOD_BUTTON_CLICK_OFF, SoundEffects.NETHER_WOOD_BUTTON_CLICK_ON));
        WARPED = register(new BlockSetType("warped", true, true, true, BlockSetType.a.EVERYTHING, SoundEffectType.NETHER_WOOD, SoundEffects.NETHER_WOOD_DOOR_CLOSE, SoundEffects.NETHER_WOOD_DOOR_OPEN, SoundEffects.NETHER_WOOD_TRAPDOOR_CLOSE, SoundEffects.NETHER_WOOD_TRAPDOOR_OPEN, SoundEffects.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEffects.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEffects.NETHER_WOOD_BUTTON_CLICK_OFF, SoundEffects.NETHER_WOOD_BUTTON_CLICK_ON));
        MANGROVE = register(new BlockSetType("mangrove"));
        BAMBOO = register(new BlockSetType("bamboo", true, true, true, BlockSetType.a.EVERYTHING, SoundEffectType.BAMBOO_WOOD, SoundEffects.BAMBOO_WOOD_DOOR_CLOSE, SoundEffects.BAMBOO_WOOD_DOOR_OPEN, SoundEffects.BAMBOO_WOOD_TRAPDOOR_CLOSE, SoundEffects.BAMBOO_WOOD_TRAPDOOR_OPEN, SoundEffects.BAMBOO_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEffects.BAMBOO_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEffects.BAMBOO_WOOD_BUTTON_CLICK_OFF, SoundEffects.BAMBOO_WOOD_BUTTON_CLICK_ON));
    }

    public static enum a {

        EVERYTHING, MOBS;

        private a() {}
    }
}
