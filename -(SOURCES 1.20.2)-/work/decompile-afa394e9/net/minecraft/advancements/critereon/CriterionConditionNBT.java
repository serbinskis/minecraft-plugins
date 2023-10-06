package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;

public record CriterionConditionNBT(NBTTagCompound tag) {

    public static final Codec<CriterionConditionNBT> CODEC = MojangsonParser.AS_CODEC.xmap(CriterionConditionNBT::new, CriterionConditionNBT::tag);

    public boolean matches(ItemStack itemstack) {
        return this.matches((NBTBase) itemstack.getTag());
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
                nbttagcompound.put("SelectedItem", itemstack.save(new NBTTagCompound()));
            }
        }

        return nbttagcompound;
    }
}
