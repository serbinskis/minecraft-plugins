package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;

public record BlockItemStateProperties(Map<String, String> properties) {

    public static final BlockItemStateProperties EMPTY = new BlockItemStateProperties(Map.of());
    public static final Codec<BlockItemStateProperties> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(BlockItemStateProperties::new, BlockItemStateProperties::properties);
    private static final StreamCodec<ByteBuf, Map<String, String>> PROPERTIES_STREAM_CODEC = ByteBufCodecs.map(Object2ObjectOpenHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8);
    public static final StreamCodec<ByteBuf, BlockItemStateProperties> STREAM_CODEC = BlockItemStateProperties.PROPERTIES_STREAM_CODEC.map(BlockItemStateProperties::new, BlockItemStateProperties::properties);

    public <T extends Comparable<T>> BlockItemStateProperties with(IBlockState<T> iblockstate, T t0) {
        return new BlockItemStateProperties(SystemUtils.copyAndPut(this.properties, iblockstate.getName(), iblockstate.getName(t0)));
    }

    public <T extends Comparable<T>> BlockItemStateProperties with(IBlockState<T> iblockstate, IBlockData iblockdata) {
        return this.with(iblockstate, iblockdata.getValue(iblockstate));
    }

    @Nullable
    public <T extends Comparable<T>> T get(IBlockState<T> iblockstate) {
        String s = (String) this.properties.get(iblockstate.getName());

        return s == null ? null : (Comparable) iblockstate.getValue(s).orElse((Object) null);
    }

    public IBlockData apply(IBlockData iblockdata) {
        BlockStateList<Block, IBlockData> blockstatelist = iblockdata.getBlock().getStateDefinition();
        Iterator iterator = this.properties.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = (Entry) iterator.next();
            IBlockState<?> iblockstate = blockstatelist.getProperty((String) entry.getKey());

            if (iblockstate != null) {
                iblockdata = updateState(iblockdata, iblockstate, (String) entry.getValue());
            }
        }

        return iblockdata;
    }

    private static <T extends Comparable<T>> IBlockData updateState(IBlockData iblockdata, IBlockState<T> iblockstate, String s) {
        return (IBlockData) iblockstate.getValue(s).map((comparable) -> {
            return (IBlockData) iblockdata.setValue(iblockstate, comparable);
        }).orElse(iblockdata);
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }
}
