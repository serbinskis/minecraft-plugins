package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.INamable;

public interface DataSource {

    MapCodec<DataSource> CODEC = ComponentSerialization.createLegacyComponentMatcher(new DataSource.a[]{EntityDataSource.TYPE, BlockDataSource.TYPE, StorageDataSource.TYPE}, DataSource.a::codec, DataSource::type, "source");

    Stream<NBTTagCompound> getData(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException;

    DataSource.a<?> type();

    public static record a<T extends DataSource>(MapCodec<T> codec, String id) implements INamable {

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }
}
