package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
// CraftBukkit end

public record Ignite(LevelBasedValue duration) implements EnchantmentEntityEffect {

    public static final MapCodec<Ignite> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LevelBasedValue.CODEC.fieldOf("duration").forGetter((ignite) -> {
            return ignite.duration;
        })).apply(instance, Ignite::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        // CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
        EntityCombustEvent entityCombustEvent;
        if (enchantediteminuse.owner() != null) {
            entityCombustEvent = new EntityCombustByEntityEvent(enchantediteminuse.owner().getBukkitEntity(), entity.getBukkitEntity(), this.duration.calculate(i));
        } else {
            entityCombustEvent = new EntityCombustEvent(entity.getBukkitEntity(), this.duration.calculate(i));
        }

        org.bukkit.Bukkit.getPluginManager().callEvent(entityCombustEvent);
        if (entityCombustEvent.isCancelled()) {
            return;
        }

        entity.igniteForSeconds(entityCombustEvent.getDuration(), false);
        // CraftBukkit end
    }

    @Override
    public MapCodec<Ignite> codec() {
        return Ignite.CODEC;
    }
}
