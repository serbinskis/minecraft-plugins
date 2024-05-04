package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.ItemStack;

// CraftBukkit start
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.util.CraftChatMessage;
// CraftBukkit end

public record ChestLock(String key) {

    public static final ChestLock NO_LOCK = new ChestLock("");
    public static final Codec<ChestLock> CODEC = Codec.STRING.xmap(ChestLock::new, ChestLock::key);
    public static final String TAG_LOCK = "Lock";

    public boolean unlocksWith(ItemStack itemstack) {
        if (this.key.isEmpty()) {
            return true;
        } else {
            IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) itemstack.get(DataComponents.CUSTOM_NAME);

            // CraftBukkit start - SPIGOT-6307: Check for color codes if the lock contains color codes
            if (this.key.isEmpty()) return true;
            if (ichatbasecomponent != null) {
                if (this.key.indexOf(ChatColor.COLOR_CHAR) == -1) {
                    // The lock key contains no color codes, so let's ignore colors in the item display name (vanilla Minecraft behavior):
                    return this.key.equals(ichatbasecomponent.getString());
                } else {
                    // The lock key contains color codes, so let's take them into account:
                    return this.key.equals(CraftChatMessage.fromComponent(ichatbasecomponent));
                }
            }
            return false;
            // CraftBukkit end
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
