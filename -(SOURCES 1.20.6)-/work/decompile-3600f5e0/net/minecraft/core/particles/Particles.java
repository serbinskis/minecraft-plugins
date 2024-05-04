package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class Particles {

    public static final ParticleType ANGRY_VILLAGER = register("angry_villager", false);
    public static final Particle<ParticleParamBlock> BLOCK = register("block", false, ParticleParamBlock::codec, ParticleParamBlock::streamCodec);
    public static final Particle<ParticleParamBlock> BLOCK_MARKER = register("block_marker", true, ParticleParamBlock::codec, ParticleParamBlock::streamCodec);
    public static final ParticleType BUBBLE = register("bubble", false);
    public static final ParticleType CLOUD = register("cloud", false);
    public static final ParticleType CRIT = register("crit", false);
    public static final ParticleType DAMAGE_INDICATOR = register("damage_indicator", true);
    public static final ParticleType DRAGON_BREATH = register("dragon_breath", false);
    public static final ParticleType DRIPPING_LAVA = register("dripping_lava", false);
    public static final ParticleType FALLING_LAVA = register("falling_lava", false);
    public static final ParticleType LANDING_LAVA = register("landing_lava", false);
    public static final ParticleType DRIPPING_WATER = register("dripping_water", false);
    public static final ParticleType FALLING_WATER = register("falling_water", false);
    public static final Particle<ParticleParamRedstone> DUST = register("dust", false, (particle) -> {
        return ParticleParamRedstone.CODEC;
    }, (particle) -> {
        return ParticleParamRedstone.STREAM_CODEC;
    });
    public static final Particle<DustColorTransitionOptions> DUST_COLOR_TRANSITION = register("dust_color_transition", false, (particle) -> {
        return DustColorTransitionOptions.CODEC;
    }, (particle) -> {
        return DustColorTransitionOptions.STREAM_CODEC;
    });
    public static final ParticleType EFFECT = register("effect", false);
    public static final ParticleType ELDER_GUARDIAN = register("elder_guardian", true);
    public static final ParticleType ENCHANTED_HIT = register("enchanted_hit", false);
    public static final ParticleType ENCHANT = register("enchant", false);
    public static final ParticleType END_ROD = register("end_rod", false);
    public static final Particle<ColorParticleOption> ENTITY_EFFECT = register("entity_effect", false, ColorParticleOption::codec, ColorParticleOption::streamCodec);
    public static final ParticleType EXPLOSION_EMITTER = register("explosion_emitter", true);
    public static final ParticleType EXPLOSION = register("explosion", true);
    public static final ParticleType GUST = register("gust", true);
    public static final ParticleType SMALL_GUST = register("small_gust", false);
    public static final ParticleType GUST_EMITTER_LARGE = register("gust_emitter_large", true);
    public static final ParticleType GUST_EMITTER_SMALL = register("gust_emitter_small", true);
    public static final ParticleType SONIC_BOOM = register("sonic_boom", true);
    public static final Particle<ParticleParamBlock> FALLING_DUST = register("falling_dust", false, ParticleParamBlock::codec, ParticleParamBlock::streamCodec);
    public static final ParticleType FIREWORK = register("firework", false);
    public static final ParticleType FISHING = register("fishing", false);
    public static final ParticleType FLAME = register("flame", false);
    public static final ParticleType INFESTED = register("infested", false);
    public static final ParticleType CHERRY_LEAVES = register("cherry_leaves", false);
    public static final ParticleType SCULK_SOUL = register("sculk_soul", false);
    public static final Particle<SculkChargeParticleOptions> SCULK_CHARGE = register("sculk_charge", true, (particle) -> {
        return SculkChargeParticleOptions.CODEC;
    }, (particle) -> {
        return SculkChargeParticleOptions.STREAM_CODEC;
    });
    public static final ParticleType SCULK_CHARGE_POP = register("sculk_charge_pop", true);
    public static final ParticleType SOUL_FIRE_FLAME = register("soul_fire_flame", false);
    public static final ParticleType SOUL = register("soul", false);
    public static final ParticleType FLASH = register("flash", false);
    public static final ParticleType HAPPY_VILLAGER = register("happy_villager", false);
    public static final ParticleType COMPOSTER = register("composter", false);
    public static final ParticleType HEART = register("heart", false);
    public static final ParticleType INSTANT_EFFECT = register("instant_effect", false);
    public static final Particle<ParticleParamItem> ITEM = register("item", false, ParticleParamItem::codec, ParticleParamItem::streamCodec);
    public static final Particle<VibrationParticleOption> VIBRATION = register("vibration", true, (particle) -> {
        return VibrationParticleOption.CODEC;
    }, (particle) -> {
        return VibrationParticleOption.STREAM_CODEC;
    });
    public static final ParticleType ITEM_SLIME = register("item_slime", false);
    public static final ParticleType ITEM_COBWEB = register("item_cobweb", false);
    public static final ParticleType ITEM_SNOWBALL = register("item_snowball", false);
    public static final ParticleType LARGE_SMOKE = register("large_smoke", false);
    public static final ParticleType LAVA = register("lava", false);
    public static final ParticleType MYCELIUM = register("mycelium", false);
    public static final ParticleType NOTE = register("note", false);
    public static final ParticleType POOF = register("poof", true);
    public static final ParticleType PORTAL = register("portal", false);
    public static final ParticleType RAIN = register("rain", false);
    public static final ParticleType SMOKE = register("smoke", false);
    public static final ParticleType WHITE_SMOKE = register("white_smoke", false);
    public static final ParticleType SNEEZE = register("sneeze", false);
    public static final ParticleType SPIT = register("spit", true);
    public static final ParticleType SQUID_INK = register("squid_ink", true);
    public static final ParticleType SWEEP_ATTACK = register("sweep_attack", true);
    public static final ParticleType TOTEM_OF_UNDYING = register("totem_of_undying", false);
    public static final ParticleType UNDERWATER = register("underwater", false);
    public static final ParticleType SPLASH = register("splash", false);
    public static final ParticleType WITCH = register("witch", false);
    public static final ParticleType BUBBLE_POP = register("bubble_pop", false);
    public static final ParticleType CURRENT_DOWN = register("current_down", false);
    public static final ParticleType BUBBLE_COLUMN_UP = register("bubble_column_up", false);
    public static final ParticleType NAUTILUS = register("nautilus", false);
    public static final ParticleType DOLPHIN = register("dolphin", false);
    public static final ParticleType CAMPFIRE_COSY_SMOKE = register("campfire_cosy_smoke", true);
    public static final ParticleType CAMPFIRE_SIGNAL_SMOKE = register("campfire_signal_smoke", true);
    public static final ParticleType DRIPPING_HONEY = register("dripping_honey", false);
    public static final ParticleType FALLING_HONEY = register("falling_honey", false);
    public static final ParticleType LANDING_HONEY = register("landing_honey", false);
    public static final ParticleType FALLING_NECTAR = register("falling_nectar", false);
    public static final ParticleType FALLING_SPORE_BLOSSOM = register("falling_spore_blossom", false);
    public static final ParticleType ASH = register("ash", false);
    public static final ParticleType CRIMSON_SPORE = register("crimson_spore", false);
    public static final ParticleType WARPED_SPORE = register("warped_spore", false);
    public static final ParticleType SPORE_BLOSSOM_AIR = register("spore_blossom_air", false);
    public static final ParticleType DRIPPING_OBSIDIAN_TEAR = register("dripping_obsidian_tear", false);
    public static final ParticleType FALLING_OBSIDIAN_TEAR = register("falling_obsidian_tear", false);
    public static final ParticleType LANDING_OBSIDIAN_TEAR = register("landing_obsidian_tear", false);
    public static final ParticleType REVERSE_PORTAL = register("reverse_portal", false);
    public static final ParticleType WHITE_ASH = register("white_ash", false);
    public static final ParticleType SMALL_FLAME = register("small_flame", false);
    public static final ParticleType SNOWFLAKE = register("snowflake", false);
    public static final ParticleType DRIPPING_DRIPSTONE_LAVA = register("dripping_dripstone_lava", false);
    public static final ParticleType FALLING_DRIPSTONE_LAVA = register("falling_dripstone_lava", false);
    public static final ParticleType DRIPPING_DRIPSTONE_WATER = register("dripping_dripstone_water", false);
    public static final ParticleType FALLING_DRIPSTONE_WATER = register("falling_dripstone_water", false);
    public static final ParticleType GLOW_SQUID_INK = register("glow_squid_ink", true);
    public static final ParticleType GLOW = register("glow", true);
    public static final ParticleType WAX_ON = register("wax_on", true);
    public static final ParticleType WAX_OFF = register("wax_off", true);
    public static final ParticleType ELECTRIC_SPARK = register("electric_spark", true);
    public static final ParticleType SCRAPE = register("scrape", true);
    public static final Particle<ShriekParticleOption> SHRIEK = register("shriek", false, (particle) -> {
        return ShriekParticleOption.CODEC;
    }, (particle) -> {
        return ShriekParticleOption.STREAM_CODEC;
    });
    public static final ParticleType EGG_CRACK = register("egg_crack", false);
    public static final ParticleType DUST_PLUME = register("dust_plume", false);
    public static final ParticleType TRIAL_SPAWNER_DETECTED_PLAYER = register("trial_spawner_detection", true);
    public static final ParticleType TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS = register("trial_spawner_detection_ominous", true);
    public static final ParticleType VAULT_CONNECTION = register("vault_connection", true);
    public static final Particle<ParticleParamBlock> DUST_PILLAR = register("dust_pillar", true, ParticleParamBlock::codec, ParticleParamBlock::streamCodec);
    public static final ParticleType OMINOUS_SPAWNING = register("ominous_spawning", true);
    public static final ParticleType RAID_OMEN = register("raid_omen", false);
    public static final ParticleType TRIAL_OMEN = register("trial_omen", false);
    public static final Codec<ParticleParam> CODEC = BuiltInRegistries.PARTICLE_TYPE.byNameCodec().dispatch("type", ParticleParam::getType, Particle::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleParam> STREAM_CODEC = ByteBufCodecs.registry(Registries.PARTICLE_TYPE).dispatch(ParticleParam::getType, Particle::streamCodec);

    public Particles() {}

    private static ParticleType register(String s, boolean flag) {
        return (ParticleType) IRegistry.register(BuiltInRegistries.PARTICLE_TYPE, s, new ParticleType(flag));
    }

    private static <T extends ParticleParam> Particle<T> register(String s, boolean flag, final Function<Particle<T>, MapCodec<T>> function, final Function<Particle<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> function1) {
        return (Particle) IRegistry.register(BuiltInRegistries.PARTICLE_TYPE, s, new Particle<T>(flag) {
            @Override
            public MapCodec<T> codec() {
                return (MapCodec) function.apply(this);
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return (StreamCodec) function1.apply(this);
            }
        });
    }
}
