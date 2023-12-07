package me.wobbychip.smptweaks.nms;

import me.wobbychip.smptweaks.utils.AccessUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;

public class CustomSugarCaneBlock extends SugarCaneBlock implements BonemealableBlock {
    public static long SEED_ZERO = -Long.MAX_VALUE;
    protected CustomSugarCaneBlock(Properties settings) {
        super(settings);
        SEED_ZERO = getZeroSeed(0);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        this.randomTick(state, world, pos, RandomSource.create(SEED_ZERO));
        this.randomTick(state, world, pos, RandomSource.create(SEED_ZERO));

    }

    public static void register() {
        //Deal with this: BuiltInRegistries.BLOCK.createIntrusiveHolder(this);
        ReflectionUtils.setRegistryFrozen(BuiltInRegistries.BLOCK, false);

        Field fieldt = ReflectionUtils.getField(MappedRegistry.class, Map.class, new Object[] { "T", Holder.Reference.class }, null, null, true, Modifier.PRIVATE, Modifier.FINAL);

        try {
            AccessUtils.setAccessible(fieldt);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        ReflectionUtils.setValue(fieldt, BuiltInRegistries.BLOCK, new IdentityHashMap<>());

        //Get super block properties
        Field BlockBehaviour_properties = ReflectionUtils.getField(BlockBehaviour.class, BlockBehaviour.Properties.class, null, true);
        BlockBehaviour.Properties properties = (BlockBehaviour.Properties) ReflectionUtils.getValue(BlockBehaviour_properties, Blocks.SUGAR_CANE);
        Block CUSTOM_SUGAR_CANE = new CustomSugarCaneBlock(properties);

        //Register for bukkit - SERVER SIDE
        Field CraftMagicNumbers_BLOCK_MATERIAL = ReflectionUtils.getField(ReflectionUtils.CraftMagicNumbers, Map.class, Block.class, true);
        Map<Block, org.bukkit.Material> BLOCK_MATERIAL = (Map<Block, org.bukkit.Material>) ReflectionUtils.getValue(CraftMagicNumbers_BLOCK_MATERIAL, null);
        BLOCK_MATERIAL.put(CUSTOM_SUGAR_CANE, Material.SUGAR_CANE);

        //Register - CLIENT SIDE
        int id = Block.BLOCK_STATE_REGISTRY.getId(Blocks.SUGAR_CANE.defaultBlockState());

        //Register custom block (IDK if it will work)
        Registry.register(BuiltInRegistries.BLOCK, Blocks.SUGAR_CANE.getDescriptionId(), CUSTOM_SUGAR_CANE);
        ReflectionUtils.setRegistryFrozen(BuiltInRegistries.BLOCK, true);

        //Replace block constant inside net.minecraft.world.level.block.Blocks class
        Field field = ReflectionUtils.getField(Blocks.class, Block.class, null, null, Blocks.SUGAR_CANE, false);
        ReflectionUtils.setValue(field, null, CUSTOM_SUGAR_CANE);

    }

    public static long getZeroSeed(float value) {
        long seed = 0;

        while (seed < Long.MAX_VALUE-1) {
            float next = RandomSource.create(seed).nextFloat();
            if (Math.abs(next-value) < Math.ulp(1)) { return seed; } else { seed++; }
        }

        return -Long.MAX_VALUE;
    }
}

