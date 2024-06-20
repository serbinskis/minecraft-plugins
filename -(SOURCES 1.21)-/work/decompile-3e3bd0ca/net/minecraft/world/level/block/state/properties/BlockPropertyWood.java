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

public record BlockPropertyWood(String name, BlockSetType setType, SoundEffectType soundType, SoundEffectType hangingSignSoundType, SoundEffect fenceGateClose, SoundEffect fenceGateOpen) {

    private static final Map<String, BlockPropertyWood> TYPES = new Object2ObjectArrayMap();
    public static final Codec<BlockPropertyWood> CODEC;
    public static final BlockPropertyWood OAK;
    public static final BlockPropertyWood SPRUCE;
    public static final BlockPropertyWood BIRCH;
    public static final BlockPropertyWood ACACIA;
    public static final BlockPropertyWood CHERRY;
    public static final BlockPropertyWood JUNGLE;
    public static final BlockPropertyWood DARK_OAK;
    public static final BlockPropertyWood CRIMSON;
    public static final BlockPropertyWood WARPED;
    public static final BlockPropertyWood MANGROVE;
    public static final BlockPropertyWood BAMBOO;

    public BlockPropertyWood(String s, BlockSetType blocksettype) {
        this(s, blocksettype, SoundEffectType.WOOD, SoundEffectType.HANGING_SIGN, SoundEffects.FENCE_GATE_CLOSE, SoundEffects.FENCE_GATE_OPEN);
    }

    private static BlockPropertyWood register(BlockPropertyWood blockpropertywood) {
        BlockPropertyWood.TYPES.put(blockpropertywood.name(), blockpropertywood);
        return blockpropertywood;
    }

    public static Stream<BlockPropertyWood> values() {
        return BlockPropertyWood.TYPES.values().stream();
    }

    static {
        Function function = BlockPropertyWood::name;
        Map map = BlockPropertyWood.TYPES;

        Objects.requireNonNull(map);
        CODEC = Codec.stringResolver(function, map::get);
        OAK = register(new BlockPropertyWood("oak", BlockSetType.OAK));
        SPRUCE = register(new BlockPropertyWood("spruce", BlockSetType.SPRUCE));
        BIRCH = register(new BlockPropertyWood("birch", BlockSetType.BIRCH));
        ACACIA = register(new BlockPropertyWood("acacia", BlockSetType.ACACIA));
        CHERRY = register(new BlockPropertyWood("cherry", BlockSetType.CHERRY, SoundEffectType.CHERRY_WOOD, SoundEffectType.CHERRY_WOOD_HANGING_SIGN, SoundEffects.CHERRY_WOOD_FENCE_GATE_CLOSE, SoundEffects.CHERRY_WOOD_FENCE_GATE_OPEN));
        JUNGLE = register(new BlockPropertyWood("jungle", BlockSetType.JUNGLE));
        DARK_OAK = register(new BlockPropertyWood("dark_oak", BlockSetType.DARK_OAK));
        CRIMSON = register(new BlockPropertyWood("crimson", BlockSetType.CRIMSON, SoundEffectType.NETHER_WOOD, SoundEffectType.NETHER_WOOD_HANGING_SIGN, SoundEffects.NETHER_WOOD_FENCE_GATE_CLOSE, SoundEffects.NETHER_WOOD_FENCE_GATE_OPEN));
        WARPED = register(new BlockPropertyWood("warped", BlockSetType.WARPED, SoundEffectType.NETHER_WOOD, SoundEffectType.NETHER_WOOD_HANGING_SIGN, SoundEffects.NETHER_WOOD_FENCE_GATE_CLOSE, SoundEffects.NETHER_WOOD_FENCE_GATE_OPEN));
        MANGROVE = register(new BlockPropertyWood("mangrove", BlockSetType.MANGROVE));
        BAMBOO = register(new BlockPropertyWood("bamboo", BlockSetType.BAMBOO, SoundEffectType.BAMBOO_WOOD, SoundEffectType.BAMBOO_WOOD_HANGING_SIGN, SoundEffects.BAMBOO_WOOD_FENCE_GATE_CLOSE, SoundEffects.BAMBOO_WOOD_FENCE_GATE_OPEN));
    }
}
