package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.ItemStack;

public record ChestLock(String key) {

    public static final ChestLock NO_LOCK = new ChestLock("");
    public static final Codec<ChestLock> CODEC = Codec.STRING.xmap(ChestLock::new, ChestLock::key);
    public static final String TAG_LOCK = "Lock";

    public boolean unlocksWith(ItemStack itemstack) {
        if (this.key.isEmpty()) {
            return true;
        } else {
            IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) itemstack.get(DataComponents.CUSTOM_NAME);

            return ichatbasecomponent != null && this.key.equals(ichatbasecomponent.getString());
        }
    }

    public void addToTag(NBTTagCompound nbttagcompound) {
        if (!this.key.isEmpty()) {
            nbttagcompound.putString("Lock", this.key);
        }

    }

    public static ChestLock fromTag(NBTTagCompound nbttagcompound) {
        return nbttagcompound.contains("Lock", 8) ? new ChestLock(nbttagcompound.getString("Lock")) : ChestLock.NO_LOCK;
    }
}
