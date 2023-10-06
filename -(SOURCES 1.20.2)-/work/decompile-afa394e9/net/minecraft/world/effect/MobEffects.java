package net.minecraft.world.effect;

import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;

public class MobEffects {

    private static final int DARKNESS_EFFECT_FACTOR_PADDING_DURATION_TICKS = 22;
    public static final MobEffectList MOVEMENT_SPEED = register("speed", (new MobEffectList(MobEffectInfo.BENEFICIAL, 3402751)).addAttributeModifier(GenericAttributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", 0.20000000298023224D, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final MobEffectList MOVEMENT_SLOWDOWN = register("slowness", (new MobEffectList(MobEffectInfo.HARMFUL, 9154528)).addAttributeModifier(GenericAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15000000596046448D, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final MobEffectList DIG_SPEED = register("haste", (new MobEffectList(MobEffectInfo.BENEFICIAL, 14270531)).addAttributeModifier(GenericAttributes.ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.10000000149011612D, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final MobEffectList DIG_SLOWDOWN = register("mining_fatigue", (new MobEffectList(MobEffectInfo.HARMFUL, 4866583)).addAttributeModifier(GenericAttributes.ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.10000000149011612D, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final MobEffectList DAMAGE_BOOST = register("strength", (new MobEffectList(MobEffectInfo.BENEFICIAL, 16762624)).addAttributeModifier(GenericAttributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 3.0D, AttributeModifier.Operation.ADDITION));
    public static final MobEffectList HEAL = register("instant_health", new HealOrHarmMobEffect(MobEffectInfo.BENEFICIAL, 16262179, false));
    public static final MobEffectList HARM = register("instant_damage", new HealOrHarmMobEffect(MobEffectInfo.HARMFUL, 11101546, true));
    public static final MobEffectList JUMP = register("jump_boost", new MobEffectList(MobEffectInfo.BENEFICIAL, 16646020));
    public static final MobEffectList CONFUSION = register("nausea", new MobEffectList(MobEffectInfo.HARMFUL, 5578058));
    public static final MobEffectList REGENERATION = register("regeneration", new RegenerationMobEffect(MobEffectInfo.BENEFICIAL, 13458603));
    public static final MobEffectList DAMAGE_RESISTANCE = register("resistance", new MobEffectList(MobEffectInfo.BENEFICIAL, 9520880));
    public static final MobEffectList FIRE_RESISTANCE = register("fire_resistance", new MobEffectList(MobEffectInfo.BENEFICIAL, 16750848));
    public static final MobEffectList WATER_BREATHING = register("water_breathing", new MobEffectList(MobEffectInfo.BENEFICIAL, 10017472));
    public static final MobEffectList INVISIBILITY = register("invisibility", new MobEffectList(MobEffectInfo.BENEFICIAL, 16185078));
    public static final MobEffectList BLINDNESS = register("blindness", new MobEffectList(MobEffectInfo.HARMFUL, 2039587));
    public static final MobEffectList NIGHT_VISION = register("night_vision", new MobEffectList(MobEffectInfo.BENEFICIAL, 12779366));
    public static final MobEffectList HUNGER = register("hunger", new HungerMobEffect(MobEffectInfo.HARMFUL, 5797459));
    public static final MobEffectList WEAKNESS = register("weakness", (new MobEffectList(MobEffectInfo.HARMFUL, 4738376)).addAttributeModifier(GenericAttributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -4.0D, AttributeModifier.Operation.ADDITION));
    public static final MobEffectList POISON = register("poison", new PoisonMobEffect(MobEffectInfo.HARMFUL, 8889187));
    public static final MobEffectList WITHER = register("wither", new WitherMobEffect(MobEffectInfo.HARMFUL, 7561558));
    public static final MobEffectList HEALTH_BOOST = register("health_boost", (new MobEffectList(MobEffectInfo.BENEFICIAL, 16284963)).addAttributeModifier(GenericAttributes.MAX_HEALTH, "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", 4.0D, AttributeModifier.Operation.ADDITION));
    public static final MobEffectList ABSORPTION = register("absorption", (new AbsorptionMobEffect(MobEffectInfo.BENEFICIAL, 2445989)).addAttributeModifier(GenericAttributes.MAX_ABSORPTION, "EAE29CF0-701E-4ED6-883A-96F798F3DAB5", 4.0D, AttributeModifier.Operation.ADDITION));
    public static final MobEffectList SATURATION = register("saturation", new SaturationMobEffect(MobEffectInfo.BENEFICIAL, 16262179));
    public static final MobEffectList GLOWING = register("glowing", new MobEffectList(MobEffectInfo.NEUTRAL, 9740385));
    public static final MobEffectList LEVITATION = register("levitation", new MobEffectList(MobEffectInfo.HARMFUL, 13565951));
    public static final MobEffectList LUCK = register("luck", (new MobEffectList(MobEffectInfo.BENEFICIAL, 5882118)).addAttributeModifier(GenericAttributes.LUCK, "03C3C89D-7037-4B42-869F-B146BCB64D2E", 1.0D, AttributeModifier.Operation.ADDITION));
    public static final MobEffectList UNLUCK = register("unluck", (new MobEffectList(MobEffectInfo.HARMFUL, 12624973)).addAttributeModifier(GenericAttributes.LUCK, "CC5AF142-2BD2-4215-B636-2605AED11727", -1.0D, AttributeModifier.Operation.ADDITION));
    public static final MobEffectList SLOW_FALLING = register("slow_falling", new MobEffectList(MobEffectInfo.BENEFICIAL, 15978425));
    public static final MobEffectList CONDUIT_POWER = register("conduit_power", new MobEffectList(MobEffectInfo.BENEFICIAL, 1950417));
    public static final MobEffectList DOLPHINS_GRACE = register("dolphins_grace", new MobEffectList(MobEffectInfo.BENEFICIAL, 8954814));
    public static final MobEffectList BAD_OMEN = register("bad_omen", new BadOmenMobEffect(MobEffectInfo.NEUTRAL, 745784));
    public static final MobEffectList HERO_OF_THE_VILLAGE = register("hero_of_the_village", new MobEffectList(MobEffectInfo.BENEFICIAL, 4521796));
    public static final MobEffectList DARKNESS = register("darkness", (new MobEffectList(MobEffectInfo.HARMFUL, 2696993)).setFactorDataFactory(() -> {
        return new MobEffect.a(22);
    }));

    public MobEffects() {}

    private static MobEffectList register(String s, MobEffectList mobeffectlist) {
        return (MobEffectList) IRegistry.register(BuiltInRegistries.MOB_EFFECT, s, mobeffectlist);
    }
}
