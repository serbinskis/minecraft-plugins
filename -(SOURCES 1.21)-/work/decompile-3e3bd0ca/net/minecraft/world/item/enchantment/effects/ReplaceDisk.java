package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProvider;
import net.minecraft.world.phys.Vec3D;

public record ReplaceDisk(LevelBasedValue radius, LevelBasedValue height, BaseBlockPosition offset, Optional<BlockPredicate> predicate, WorldGenFeatureStateProvider blockState, Optional<Holder<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect {

    public static final MapCodec<ReplaceDisk> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LevelBasedValue.CODEC.fieldOf("radius").forGetter(ReplaceDisk::radius), LevelBasedValue.CODEC.fieldOf("height").forGetter(ReplaceDisk::height), BaseBlockPosition.CODEC.optionalFieldOf("offset", BaseBlockPosition.ZERO).forGetter(ReplaceDisk::offset), BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceDisk::predicate), WorldGenFeatureStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceDisk::blockState), GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceDisk::triggerGameEvent)).apply(instance, ReplaceDisk::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        BlockPosition blockposition = BlockPosition.containing(vec3d).offset(this.offset);
        RandomSource randomsource = entity.getRandom();
        int j = (int) this.radius.calculate(i);
        int k = (int) this.height.calculate(i);
        Iterator iterator = BlockPosition.betweenClosed(blockposition.offset(-j, 0, -j), blockposition.offset(j, Math.min(k - 1, 0), j)).iterator();

        while (iterator.hasNext()) {
            BlockPosition blockposition1 = (BlockPosition) iterator.next();

            if (blockposition1.distToCenterSqr(vec3d.x(), (double) blockposition1.getY() + 0.5D, vec3d.z()) < (double) MathHelper.square(j) && (Boolean) this.predicate.map((blockpredicate) -> {
                return blockpredicate.test(worldserver, blockposition1);
            }).orElse(true) && worldserver.setBlockAndUpdate(blockposition1, this.blockState.getState(randomsource, blockposition1))) {
                this.triggerGameEvent.ifPresent((holder) -> {
                    worldserver.gameEvent(entity, holder, blockposition1);
                });
            }
        }

    }

    @Override
    public MapCodec<ReplaceDisk> codec() {
        return ReplaceDisk.CODEC;
    }
}
