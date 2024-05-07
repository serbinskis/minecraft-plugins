package net.minecraft.world.level;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public interface Spawner {

    void setEntityId(EntityTypes<?> entitytypes, RandomSource randomsource);

    static void appendHoverText(ItemStack itemstack, List<IChatBaseComponent> list, String s) {
        IChatBaseComponent ichatbasecomponent = getSpawnEntityDisplayName(itemstack, s);

        if (ichatbasecomponent != null) {
            list.add(ichatbasecomponent);
        } else {
            list.add(CommonComponents.EMPTY);
            list.add(IChatBaseComponent.translatable("block.minecraft.spawner.desc1").withStyle(EnumChatFormat.GRAY));
            list.add(CommonComponents.space().append((IChatBaseComponent) IChatBaseComponent.translatable("block.minecraft.spawner.desc2").withStyle(EnumChatFormat.BLUE)));
        }

    }

    @Nullable
    static IChatBaseComponent getSpawnEntityDisplayName(ItemStack itemstack, String s) {
        NBTTagCompound nbttagcompound = ((CustomData) itemstack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY)).getUnsafe();
        MinecraftKey minecraftkey = getEntityKey(nbttagcompound, s);

        return minecraftkey != null ? (IChatBaseComponent) BuiltInRegistries.ENTITY_TYPE.getOptional(minecraftkey).map((entitytypes) -> {
            return IChatBaseComponent.translatable(entitytypes.getDescriptionId()).withStyle(EnumChatFormat.GRAY);
        }).orElse((Object) null) : null;
    }

    @Nullable
    private static MinecraftKey getEntityKey(NBTTagCompound nbttagcompound, String s) {
        if (nbttagcompound.contains(s, 10)) {
            String s1 = nbttagcompound.getCompound(s).getCompound("entity").getString("id");

            return MinecraftKey.tryParse(s1);
        } else {
            return null;
        }
    }
}
