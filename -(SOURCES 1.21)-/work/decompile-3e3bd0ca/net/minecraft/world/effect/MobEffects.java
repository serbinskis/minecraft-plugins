package net.minecraft.world.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;

public class MobEffects {

    private static final int DARKNESS_EFFECT_FACTOR_PADDING_DURATION_TICKS = 22;
    public static final Holder<MobEffectList> MOVEMENT_SPEED = register("speed", (new MobEffectList(MobEffectInfo.BENEFICIAL, 3402751)).addAttributeModifier(GenericAttributes.MOVEMENT_SPEED, MinecraftKey.withDefaultNamespace("effect.speed"), 0.20000000298023224D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final Holder<MobEffectList> MOVEMENT_SLOWDOWN = register("slowness", (new MobEffectList(MobEffectInfo.HARMFUL, 9154528)).addAttributeModifier(GenericAttributes.MOVEMENT_SPEED, MinecraftKey.withDefaultNamespace("effect.slowness"), -0.15000000596046448D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final Holder<MobEffectList> DIG_SPEED = register("haste", (new MobEffectList(MobEffectInfo.BENEFICIAL, 14270531)).addAttributeModifier(GenericAttributes.ATTACK_SPEED, MinecraftKey.withDefaultNamespace("effect.haste"), 0.10000000149011612D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final Holder<MobEffectList> DIG_SLOWDOWN = register("mining_fatigue", (new MobEffectList(MobEffectInfo.HARMFUL, 4866583)).addAttributeModifier(GenericAttributes.ATTACK_SPEED, MinecraftKey.withDefaultNamespace("effect.mining_fatigue"), -0.10000000149011612D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final Holder<MobEffectList> DAMAGE_BOOST = register("strength", (new MobEffectList(MobEffectInfo.BENEFICIAL, 16762624)).addAttributeModifier(GenericAttributes.ATTACK_DAMAGE, MinecraftKey.withDefaultNamespace("effect.strength"), 3.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final Holder<MobEffectList> HEAL = register("instant_health", new HealOrHarmMobEffect(MobEffectInfo.BENEFICIAL, 16262179, false));
    public static final Holder<MobEffectList> HARM = register("instant_damage", new HealOrHarmMobEffect(MobEffectInfo.HARMFUL, 11101546, true));
    public static final Holder<MobEffectList> JUMP = register("jump_boost", (new MobEffectList(MobEffectInfo.BENEFICIAL, 16646020)).addAttributeModifier(GenericAttributes.SAFE_FALL_DISTANCE, MinecraftKey.withDefaultNamespace("effect.jump_boost"), 1.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final Holder<MobEffectList> CONFUSION = register("nausea", new MobEffectList(MobEffectInfo.HARMFUL, 5578058));
    public static final Holder<MobEffectList> REGENERATION = register("regeneration", new RegenerationMobEffect(MobEffectInfo.BENEFICIAL, 13458603));
    public static final Holder<MobEffectList> DAMAGE_RESISTANCE = register("resistance", new MobEffectList(MobEffectInfo.BENEFICIAL, 9520880));
    public static final Holder<MobEffectList> FIRE_RESISTANCE = register("fire_resistance", new MobEffectList(MobEffectInfo.BENEFICIAL, 16750848));
    public static final Holder<MobEffectList> WATER_BREATHING = register("water_breathing", new MobEffectList(MobEffectInfo.BENEFICIAL, 10017472));
    public static final Holder<MobEffectList> INVISIBILITY = register("invisibility", new MobEffectList(MobEffectInfo.BENEFICIAL, 16185078));
    public static final Holder<MobEffectList> BLINDNESS = register("blindness", new MobEffectList(MobEffectInfo.HARMFUL, 2039587));
    public static final Holder<MobEffectList> NIGHT_VISION = register("night_vision", new MobEffectList(MobEffectInfo.BENEFICIAL, 12779366));
    public static final Holder<MobEffectList> HUNGER = register("hunger", new HungerMobEffect(MobEffectInfo.HARMFUL, 5797459));
    public static final Holder<MobEffectList> WEAKNESS = register("weakness", (new MobEffectList(MobEffectInfo.HARMFUL, 4738376)).addAttributeModifier(GenericAttributes.ATTACK_DAMAGE, MinecraftKey.withDefaultNamespace("effect.weakness"), -4.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final Holder<MobEffectList> POISON = register("poison", new PoisonMobEffect(MobEffectInfo.HARMFUL, 8889187));
    public static final Holder<MobEffectList> WITHER = register("wither", new WitherMobEffect(MobEffectInfo.HARMFUL, 7561558));
    public static final Holder<MobEffectList> HEALTH_BOOST = register("health_boost", (new MobEffectList(MobEffectInfo.BENEFICIAL, 16284963)).addAttributeModifier(GenericAttributes.MAX_HEALTH, MinecraftKey.withDefaultNamespace("effect.health_boost"), 4.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final Holder<MobEffectList> ABSORPTION = register("absorption", (new AbsorptionMobEffect(MobEffectInfo.BENEFICIAL, 2445989)).addAttributeModifier(GenericAttributes.MAX_ABSORPTION, MinecraftKey.withDefaultNamespace("effect.absorption"), 4.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final Holder<MobEffectList> SATURATION = register("saturation", new SaturationMobEffect(MobEffectInfo.BENEFICIAL, 16262179));
    public static final Holder<MobEffectList> GLOWING = register("glowing", new MobEffectList(MobEffectInfo.NEUTRAL, 9740385));
    public static final Holder<MobEffectList> LEVITATION = register("levitation", new MobEffectList(MobEffectInfo.HARMFUL, 13565951));
    public static final Holder<MobEffectList> LUCK = register("luck", (new MobEffectList(MobEffectInfo.BENEFICIAL, 5882118)).addAttributeModifier(GenericAttributes.LUCK, MinecraftKey.withDefaultNamespace("effect.luck"), 1.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final Holder<MobEffectList> UNLUCK = register("unluck", (new MobEffectList(MobEffectInfo.HARMFUL, 12624973)).addAttributeModifier(GenericAttributes.LUCK, MinecraftKey.withDefaultNamespace("effect.unluck"), -1.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final Holder<MobEffectList> SLOW_FALLING = register("slow_falling", new MobEffectList(MobEffectInfo.BENEFICIAL, 15978425));
    public static final Holder<MobEffectList> CONDUIT_POWER = register("conduit_power", new MobEffectList(MobEffectInfo.BENEFICIAL, 1950417));
    public static final Holder<MobEffectList> DOLPHINS_GRACE = register("dolphins_grace", new MobEffectList(MobEffectInfo.BENEFICIAL, 8954814));
    public static final Holder<MobEffectList> BAD_OMEN = register("bad_omen", (new BadOmenMobEffect(MobEffectInfo.NEUTRAL, 745784)).withSoundOnAdded(SoundEffects.APPLY_EFFECT_BAD_OMEN));
    public static final Holder<MobEffectList> HERO_OF_THE_VILLAGE = register("hero_of_the_village", new MobEffectList(MobEffectInfo.BENEFICIAL, 4521796));
    public static final Holder<MobEffectList> DARKNESS = register("darkness", (new MobEffectList(MobEffectInfo.HARMFUL, 2696993)).setBlendDuration(22));
    public static final Holder<MobEffectList> TRIAL_OMEN = register("trial_omen", (new MobEffectList(MobEffectInfo.NEUTRAL, 1484454, Particles.TRIAL_OMEN)).withSoundOnAdded(SoundEffects.APPLY_EFFECT_TRIAL_OMEN));
    public static final Holder<MobEffectList> RAID_OMEN = register("raid_omen", (new RaidOmenMobEffect(MobEffectInfo.NEUTRAL, 14565464, Particles.RAID_OMEN)).withSoundOnAdded(SoundEffects.APPLY_EFFECT_RAID_OMEN));
    public static final Holder<MobEffectList> WIND_CHARGED = register("wind_charged", new WindChargedMobEffect(MobEffectInfo.HARMFUL, 12438015));
    public static final Holder<MobEffectList> WEAVING = register("weaving", new WeavingMobEffect(MobEffectInfo.HARMFUL, 7891290, (randomsource) -> {
        return MathHelper.randomBetweenInclusive(randomsource, 2, 3);
    }));
    public static final Holder<MobEffectList> OOZING = register("oozing", new OozingMobEffect(MobEffectInfo.HARMFUL, 10092451, (randomsource) -> {
        return 2;
    }));
    public static final Holder<MobEffectList> INFESTED = register("infested", new InfestedMobEffect(MobEffectInfo.HARMFUL, 9214860, 0.1F, (randomsource) -> {
        return MathHelper.randomBetweenInclusive(randomsource, 1, 2);
    }));

    public MobEffects() {}

    private static Holder<MobEffectList> register(String s, MobEffectList mobeffectlist) {
        return IRegistry.registerForHolder(BuiltInRegistries.MOB_EFFECT, MinecraftKey.withDefaultNamespace(s), mobeffectlist);
    }

    public static Holder<MobEffectList> bootstrap(IRegistry<MobEffectList> iregistry) {
        return MobEffects.MOVEMENT_SPEED;
    }
}
