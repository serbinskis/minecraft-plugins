package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public record SummonEntityEffect(HolderSet<EntityTypes<?>> entityTypes, boolean joinTeam) implements EnchantmentEntityEffect {

    public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(SummonEntityEffect::entityTypes), Codec.BOOL.optionalFieldOf("join_team", false).forGetter(SummonEntityEffect::joinTeam)).apply(instance, SummonEntityEffect::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        BlockPosition blockposition = BlockPosition.containing(vec3d);

        if (World.isInSpawnableBounds(blockposition)) {
            Optional<Holder<EntityTypes<?>>> optional = this.entityTypes().getRandomElement(worldserver.getRandom());

            if (!optional.isEmpty()) {
                Entity entity1 = ((EntityTypes) ((Holder) optional.get()).value()).spawn(worldserver, blockposition, EnumMobSpawn.TRIGGERED);

                if (entity1 != null) {
                    if (entity1 instanceof EntityLightning) {
                        EntityLightning entitylightning = (EntityLightning) entity1;
                        EntityLiving entityliving = enchantediteminuse.owner();

                        if (entityliving instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer) entityliving;

                            entitylightning.setCause(entityplayer);
                        }
                    }

                    if (this.joinTeam && entity.getTeam() != null) {
                        worldserver.getScoreboard().addPlayerToTeam(entity1.getScoreboardName(), entity.getTeam());
                    }

                    entity1.moveTo(vec3d.x, vec3d.y, vec3d.z, entity1.getYRot(), entity1.getXRot());
                }
            }
        }
    }

    @Override
    public MapCodec<SummonEntityEffect> codec() {
        return SummonEntityEffect.CODEC;
    }
}
