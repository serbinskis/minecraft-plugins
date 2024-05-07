package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public record CriterionConditionNBT(NBTTagCompound tag) {

    public static final Codec<CriterionConditionNBT> CODEC = MojangsonParser.LENIENT_CODEC.xmap(CriterionConditionNBT::new, CriterionConditionNBT::tag);
    public static final StreamCodec<ByteBuf, CriterionConditionNBT> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CriterionConditionNBT::new, CriterionConditionNBT::tag);

    public boolean matches(ItemStack itemstack) {
        CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        return customdata.matchedBy(this.tag);
    }

    public boolean matches(Entity entity) {
        return this.matches((NBTBase) getEntityTagToCompare(entity));
    }

    public boolean matches(@Nullable NBTBase nbtbase) {
        return nbtbase != null && GameProfileSerializer.compareNbt(this.tag, nbtbase, true);
    }

    public static NBTTagCompound getEntityTagToCompare(Entity entity) {
        NBTTagCompound nbttagcompound = entity.saveWithoutId(new NBTTagCompound());

        if (entity instanceof EntityHuman) {
            ItemStack itemstack = ((EntityHuman) entity).getInventory().getSelected();

            if (!itemstack.isEmpty()) {
                nbttagcompound.put("SelectedItem", itemstack.save(entity.registryAccess()));
            }
        }

        return nbttagcompound;
    }
}
