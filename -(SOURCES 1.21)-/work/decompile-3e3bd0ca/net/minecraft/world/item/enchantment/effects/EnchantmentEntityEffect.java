package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3D;

public interface EnchantmentEntityEffect extends EnchantmentLocationBasedEffect {

    Codec<EnchantmentEntityEffect> CODEC = BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE.byNameCodec().dispatch(EnchantmentEntityEffect::codec, Function.identity());

    static MapCodec<? extends EnchantmentEntityEffect> bootstrap(IRegistry<MapCodec<? extends EnchantmentEntityEffect>> iregistry) {
        IRegistry.register(iregistry, "all_of", AllOf.a.CODEC);
        IRegistry.register(iregistry, "apply_mob_effect", ApplyMobEffect.CODEC);
        IRegistry.register(iregistry, "damage_entity", DamageEntity.CODEC);
        IRegistry.register(iregistry, "damage_item", DamageItem.CODEC);
        IRegistry.register(iregistry, "explode", ExplodeEffect.CODEC);
        IRegistry.register(iregistry, "ignite", Ignite.CODEC);
        IRegistry.register(iregistry, "play_sound", PlaySoundEffect.CODEC);
        IRegistry.register(iregistry, "replace_block", ReplaceBlock.CODEC);
        IRegistry.register(iregistry, "replace_disk", ReplaceDisk.CODEC);
        IRegistry.register(iregistry, "run_function", RunFunction.CODEC);
        IRegistry.register(iregistry, "set_block_properties", SetBlockProperties.CODEC);
        IRegistry.register(iregistry, "spawn_particles", SpawnParticlesEffect.CODEC);
        return (MapCodec) IRegistry.register(iregistry, "summon_entity", SummonEntityEffect.CODEC);
    }

    void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d);

    @Override
    default void onChangedBlock(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d, boolean flag) {
        this.apply(worldserver, i, enchantediteminuse, entity, vec3d);
    }

    @Override
    MapCodec<? extends EnchantmentEntityEffect> codec();
}
