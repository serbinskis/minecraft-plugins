package net.minecraft.advancements;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

// CraftBukkit start
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
// CraftBukkit end

public record AdvancementHolder(MinecraftKey id, Advancement value) {

    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceLocation(this.id);
        this.value.write(packetdataserializer);
    }

    public static AdvancementHolder read(PacketDataSerializer packetdataserializer) {
        return new AdvancementHolder(packetdataserializer.readResourceLocation(), Advancement.read(packetdataserializer));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof AdvancementHolder) {
                AdvancementHolder advancementholder = (AdvancementHolder) object;

                if (this.id.equals(advancementholder.id)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return this.id.toString();
    }

    // CraftBukkit start
    public final org.bukkit.advancement.Advancement toBukkit() {
        return new CraftAdvancement(this);
    }
    // CraftBukkit end
}
