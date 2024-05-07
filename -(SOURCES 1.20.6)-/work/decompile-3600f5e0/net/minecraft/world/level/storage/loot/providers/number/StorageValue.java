package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTNumber;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public record StorageValue(MinecraftKey storage, ArgumentNBTKey.g path) implements NumberProvider {

    public static final MapCodec<StorageValue> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("storage").forGetter(StorageValue::storage), ArgumentNBTKey.g.CODEC.fieldOf("path").forGetter(StorageValue::path)).apply(instance, StorageValue::new);
    });

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.STORAGE;
    }

    private Optional<NBTNumber> getNumericTag(LootTableInfo loottableinfo) {
        NBTTagCompound nbttagcompound = loottableinfo.getLevel().getServer().getCommandStorage().get(this.storage);

        try {
            List<NBTBase> list = this.path.get(nbttagcompound);

            if (list.size() == 1) {
                Object object = list.get(0);

                if (object instanceof NBTNumber) {
                    NBTNumber nbtnumber = (NBTNumber) object;

                    return Optional.of(nbtnumber);
                }
            }
        } catch (CommandSyntaxException commandsyntaxexception) {
            ;
        }

        return Optional.empty();
    }

    @Override
    public float getFloat(LootTableInfo loottableinfo) {
        return (Float) this.getNumericTag(loottableinfo).map(NBTNumber::getAsFloat).orElse(0.0F);
    }

    @Override
    public int getInt(LootTableInfo loottableinfo) {
        return (Integer) this.getNumericTag(loottableinfo).map(NBTNumber::getAsInt).orElse(0);
    }
}
