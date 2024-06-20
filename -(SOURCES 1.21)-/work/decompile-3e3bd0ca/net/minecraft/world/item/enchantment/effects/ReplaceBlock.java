package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProvider;
import net.minecraft.world.phys.Vec3D;

public record ReplaceBlock(BaseBlockPosition offset, Optional<BlockPredicate> predicate, WorldGenFeatureStateProvider blockState, Optional<Holder<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect {

    public static final MapCodec<ReplaceBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BaseBlockPosition.CODEC.optionalFieldOf("offset", BaseBlockPosition.ZERO).forGetter(ReplaceBlock::offset), BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceBlock::predicate), WorldGenFeatureStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceBlock::blockState), GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceBlock::triggerGameEvent)).apply(instance, ReplaceBlock::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        BlockPosition blockposition = BlockPosition.containing(vec3d).offset(this.offset);

        if ((Boolean) this.predicate.map((blockpredicate) -> {
            return blockpredicate.test(worldserver, blockposition);
        }).orElse(true) && worldserver.setBlockAndUpdate(blockposition, this.blockState.getState(entity.getRandom(), blockposition))) {
            this.triggerGameEvent.ifPresent((holder) -> {
                worldserver.gameEvent(entity, holder, blockposition);
            });
        }

    }

    @Override
    public MapCodec<ReplaceBlock> codec() {
        return ReplaceBlock.CODEC;
    }
}
