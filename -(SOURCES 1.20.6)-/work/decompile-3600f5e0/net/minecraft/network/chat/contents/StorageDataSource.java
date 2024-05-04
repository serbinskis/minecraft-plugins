package net.minecraft.network.chat.contents;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;

public record StorageDataSource(MinecraftKey id) implements DataSource {

    public static final MapCodec<StorageDataSource> SUB_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("storage").forGetter(StorageDataSource::id)).apply(instance, StorageDataSource::new);
    });
    public static final DataSource.a<StorageDataSource> TYPE = new DataSource.a<>(StorageDataSource.SUB_CODEC, "storage");

    @Override
    public Stream<NBTTagCompound> getData(CommandListenerWrapper commandlistenerwrapper) {
        NBTTagCompound nbttagcompound = commandlistenerwrapper.getServer().getCommandStorage().get(this.id);

        return Stream.of(nbttagcompound);
    }

    @Override
    public DataSource.a<?> type() {
        return StorageDataSource.TYPE;
    }

    public String toString() {
        return "storage=" + String.valueOf(this.id);
    }
}
