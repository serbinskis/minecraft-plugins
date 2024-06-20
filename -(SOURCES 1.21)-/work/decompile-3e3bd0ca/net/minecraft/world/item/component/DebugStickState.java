package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IBlockState;

public record DebugStickState(Map<Holder<Block>, IBlockState<?>> properties) {

    public static final DebugStickState EMPTY = new DebugStickState(Map.of());
    public static final Codec<DebugStickState> CODEC = Codec.dispatchedMap(BuiltInRegistries.BLOCK.holderByNameCodec(), (holder) -> {
        return Codec.STRING.comapFlatMap((s) -> {
            IBlockState<?> iblockstate = ((Block) holder.value()).getStateDefinition().getProperty(s);

            return iblockstate != null ? DataResult.success(iblockstate) : DataResult.error(() -> {
                String s1 = holder.getRegisteredName();

                return "No property on " + s1 + " with name: " + s;
            });
        }, IBlockState::getName);
    }).xmap(DebugStickState::new, DebugStickState::properties);

    public DebugStickState withProperty(Holder<Block> holder, IBlockState<?> iblockstate) {
        return new DebugStickState(SystemUtils.copyAndPut(this.properties, holder, iblockstate));
    }
}
