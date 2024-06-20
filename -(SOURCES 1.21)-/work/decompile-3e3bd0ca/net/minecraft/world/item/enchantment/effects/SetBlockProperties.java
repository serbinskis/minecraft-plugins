package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public record SetBlockProperties(BlockItemStateProperties properties, BaseBlockPosition offset, Optional<Holder<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect {

    public static final MapCodec<SetBlockProperties> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockItemStateProperties.CODEC.fieldOf("properties").forGetter(SetBlockProperties::properties), BaseBlockPosition.CODEC.optionalFieldOf("offset", BaseBlockPosition.ZERO).forGetter(SetBlockProperties::offset), GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(SetBlockProperties::triggerGameEvent)).apply(instance, SetBlockProperties::new);
    });

    public SetBlockProperties(BlockItemStateProperties blockitemstateproperties) {
        this(blockitemstateproperties, BaseBlockPosition.ZERO, Optional.of(GameEvent.BLOCK_CHANGE));
    }

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        BlockPosition blockposition = BlockPosition.containing(vec3d).offset(this.offset);
        IBlockData iblockdata = entity.level().getBlockState(blockposition);
        IBlockData iblockdata1 = this.properties.apply(iblockdata);

        if (!iblockdata.equals(iblockdata1) && entity.level().setBlock(blockposition, iblockdata1, 3)) {
            this.triggerGameEvent.ifPresent((holder) -> {
                worldserver.gameEvent(entity, holder, blockposition);
            });
        }

    }

    @Override
    public MapCodec<SetBlockProperties> codec() {
        return SetBlockProperties.CODEC;
    }
}
