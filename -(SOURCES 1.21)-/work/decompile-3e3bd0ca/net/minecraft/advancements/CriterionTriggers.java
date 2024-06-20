package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.AnyBlockInteractionTrigger;
import net.minecraft.advancements.critereon.CriterionSlideDownBlock;
import net.minecraft.advancements.critereon.CriterionTriggerBeeNestDestroyed;
import net.minecraft.advancements.critereon.CriterionTriggerBredAnimals;
import net.minecraft.advancements.critereon.CriterionTriggerBrewedPotion;
import net.minecraft.advancements.critereon.CriterionTriggerChangedDimension;
import net.minecraft.advancements.critereon.CriterionTriggerChanneledLightning;
import net.minecraft.advancements.critereon.CriterionTriggerConstructBeacon;
import net.minecraft.advancements.critereon.CriterionTriggerConsumeItem;
import net.minecraft.advancements.critereon.CriterionTriggerCuredZombieVillager;
import net.minecraft.advancements.critereon.CriterionTriggerEffectsChanged;
import net.minecraft.advancements.critereon.CriterionTriggerEnchantedItem;
import net.minecraft.advancements.critereon.CriterionTriggerEnterBlock;
import net.minecraft.advancements.critereon.CriterionTriggerEntityHurtPlayer;
import net.minecraft.advancements.critereon.CriterionTriggerFilledBucket;
import net.minecraft.advancements.critereon.CriterionTriggerFishingRodHooked;
import net.minecraft.advancements.critereon.CriterionTriggerImpossible;
import net.minecraft.advancements.critereon.CriterionTriggerInventoryChanged;
import net.minecraft.advancements.critereon.CriterionTriggerItemDurabilityChanged;
import net.minecraft.advancements.critereon.CriterionTriggerKilled;
import net.minecraft.advancements.critereon.CriterionTriggerKilledByCrossbow;
import net.minecraft.advancements.critereon.CriterionTriggerLevitation;
import net.minecraft.advancements.critereon.CriterionTriggerPlayerGeneratesContainerLoot;
import net.minecraft.advancements.critereon.CriterionTriggerPlayerHurtEntity;
import net.minecraft.advancements.critereon.CriterionTriggerPlayerInteractedWithEntity;
import net.minecraft.advancements.critereon.CriterionTriggerRecipeUnlocked;
import net.minecraft.advancements.critereon.CriterionTriggerShotCrossbow;
import net.minecraft.advancements.critereon.CriterionTriggerSummonedEntity;
import net.minecraft.advancements.critereon.CriterionTriggerTamedAnimal;
import net.minecraft.advancements.critereon.CriterionTriggerTargetHit;
import net.minecraft.advancements.critereon.CriterionTriggerUsedEnderEye;
import net.minecraft.advancements.critereon.CriterionTriggerUsedTotem;
import net.minecraft.advancements.critereon.CriterionTriggerVillagerTrade;
import net.minecraft.advancements.critereon.DefaultBlockInteractionTrigger;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.FallAfterExplosionTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.advancements.critereon.PickedUpItemTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public class CriterionTriggers {

    public static final Codec<CriterionTrigger<?>> CODEC = BuiltInRegistries.TRIGGER_TYPES.byNameCodec();
    public static final CriterionTriggerImpossible IMPOSSIBLE = (CriterionTriggerImpossible) register("impossible", new CriterionTriggerImpossible());
    public static final CriterionTriggerKilled PLAYER_KILLED_ENTITY = (CriterionTriggerKilled) register("player_killed_entity", new CriterionTriggerKilled());
    public static final CriterionTriggerKilled ENTITY_KILLED_PLAYER = (CriterionTriggerKilled) register("entity_killed_player", new CriterionTriggerKilled());
    public static final CriterionTriggerEnterBlock ENTER_BLOCK = (CriterionTriggerEnterBlock) register("enter_block", new CriterionTriggerEnterBlock());
    public static final CriterionTriggerInventoryChanged INVENTORY_CHANGED = (CriterionTriggerInventoryChanged) register("inventory_changed", new CriterionTriggerInventoryChanged());
    public static final CriterionTriggerRecipeUnlocked RECIPE_UNLOCKED = (CriterionTriggerRecipeUnlocked) register("recipe_unlocked", new CriterionTriggerRecipeUnlocked());
    public static final CriterionTriggerPlayerHurtEntity PLAYER_HURT_ENTITY = (CriterionTriggerPlayerHurtEntity) register("player_hurt_entity", new CriterionTriggerPlayerHurtEntity());
    public static final CriterionTriggerEntityHurtPlayer ENTITY_HURT_PLAYER = (CriterionTriggerEntityHurtPlayer) register("entity_hurt_player", new CriterionTriggerEntityHurtPlayer());
    public static final CriterionTriggerEnchantedItem ENCHANTED_ITEM = (CriterionTriggerEnchantedItem) register("enchanted_item", new CriterionTriggerEnchantedItem());
    public static final CriterionTriggerFilledBucket FILLED_BUCKET = (CriterionTriggerFilledBucket) register("filled_bucket", new CriterionTriggerFilledBucket());
    public static final CriterionTriggerBrewedPotion BREWED_POTION = (CriterionTriggerBrewedPotion) register("brewed_potion", new CriterionTriggerBrewedPotion());
    public static final CriterionTriggerConstructBeacon CONSTRUCT_BEACON = (CriterionTriggerConstructBeacon) register("construct_beacon", new CriterionTriggerConstructBeacon());
    public static final CriterionTriggerUsedEnderEye USED_ENDER_EYE = (CriterionTriggerUsedEnderEye) register("used_ender_eye", new CriterionTriggerUsedEnderEye());
    public static final CriterionTriggerSummonedEntity SUMMONED_ENTITY = (CriterionTriggerSummonedEntity) register("summoned_entity", new CriterionTriggerSummonedEntity());
    public static final CriterionTriggerBredAnimals BRED_ANIMALS = (CriterionTriggerBredAnimals) register("bred_animals", new CriterionTriggerBredAnimals());
    public static final PlayerTrigger LOCATION = (PlayerTrigger) register("location", new PlayerTrigger());
    public static final PlayerTrigger SLEPT_IN_BED = (PlayerTrigger) register("slept_in_bed", new PlayerTrigger());
    public static final CriterionTriggerCuredZombieVillager CURED_ZOMBIE_VILLAGER = (CriterionTriggerCuredZombieVillager) register("cured_zombie_villager", new CriterionTriggerCuredZombieVillager());
    public static final CriterionTriggerVillagerTrade TRADE = (CriterionTriggerVillagerTrade) register("villager_trade", new CriterionTriggerVillagerTrade());
    public static final CriterionTriggerItemDurabilityChanged ITEM_DURABILITY_CHANGED = (CriterionTriggerItemDurabilityChanged) register("item_durability_changed", new CriterionTriggerItemDurabilityChanged());
    public static final CriterionTriggerLevitation LEVITATION = (CriterionTriggerLevitation) register("levitation", new CriterionTriggerLevitation());
    public static final CriterionTriggerChangedDimension CHANGED_DIMENSION = (CriterionTriggerChangedDimension) register("changed_dimension", new CriterionTriggerChangedDimension());
    public static final PlayerTrigger TICK = (PlayerTrigger) register("tick", new PlayerTrigger());
    public static final CriterionTriggerTamedAnimal TAME_ANIMAL = (CriterionTriggerTamedAnimal) register("tame_animal", new CriterionTriggerTamedAnimal());
    public static final ItemUsedOnLocationTrigger PLACED_BLOCK = (ItemUsedOnLocationTrigger) register("placed_block", new ItemUsedOnLocationTrigger());
    public static final CriterionTriggerConsumeItem CONSUME_ITEM = (CriterionTriggerConsumeItem) register("consume_item", new CriterionTriggerConsumeItem());
    public static final CriterionTriggerEffectsChanged EFFECTS_CHANGED = (CriterionTriggerEffectsChanged) register("effects_changed", new CriterionTriggerEffectsChanged());
    public static final CriterionTriggerUsedTotem USED_TOTEM = (CriterionTriggerUsedTotem) register("used_totem", new CriterionTriggerUsedTotem());
    public static final DistanceTrigger NETHER_TRAVEL = (DistanceTrigger) register("nether_travel", new DistanceTrigger());
    public static final CriterionTriggerFishingRodHooked FISHING_ROD_HOOKED = (CriterionTriggerFishingRodHooked) register("fishing_rod_hooked", new CriterionTriggerFishingRodHooked());
    public static final CriterionTriggerChanneledLightning CHANNELED_LIGHTNING = (CriterionTriggerChanneledLightning) register("channeled_lightning", new CriterionTriggerChanneledLightning());
    public static final CriterionTriggerShotCrossbow SHOT_CROSSBOW = (CriterionTriggerShotCrossbow) register("shot_crossbow", new CriterionTriggerShotCrossbow());
    public static final CriterionTriggerKilledByCrossbow KILLED_BY_CROSSBOW = (CriterionTriggerKilledByCrossbow) register("killed_by_crossbow", new CriterionTriggerKilledByCrossbow());
    public static final PlayerTrigger RAID_WIN = (PlayerTrigger) register("hero_of_the_village", new PlayerTrigger());
    public static final PlayerTrigger RAID_OMEN = (PlayerTrigger) register("voluntary_exile", new PlayerTrigger());
    public static final CriterionSlideDownBlock HONEY_BLOCK_SLIDE = (CriterionSlideDownBlock) register("slide_down_block", new CriterionSlideDownBlock());
    public static final CriterionTriggerBeeNestDestroyed BEE_NEST_DESTROYED = (CriterionTriggerBeeNestDestroyed) register("bee_nest_destroyed", new CriterionTriggerBeeNestDestroyed());
    public static final CriterionTriggerTargetHit TARGET_BLOCK_HIT = (CriterionTriggerTargetHit) register("target_hit", new CriterionTriggerTargetHit());
    public static final ItemUsedOnLocationTrigger ITEM_USED_ON_BLOCK = (ItemUsedOnLocationTrigger) register("item_used_on_block", new ItemUsedOnLocationTrigger());
    public static final DefaultBlockInteractionTrigger DEFAULT_BLOCK_USE = (DefaultBlockInteractionTrigger) register("default_block_use", new DefaultBlockInteractionTrigger());
    public static final AnyBlockInteractionTrigger ANY_BLOCK_USE = (AnyBlockInteractionTrigger) register("any_block_use", new AnyBlockInteractionTrigger());
    public static final CriterionTriggerPlayerGeneratesContainerLoot GENERATE_LOOT = (CriterionTriggerPlayerGeneratesContainerLoot) register("player_generates_container_loot", new CriterionTriggerPlayerGeneratesContainerLoot());
    public static final PickedUpItemTrigger THROWN_ITEM_PICKED_UP_BY_ENTITY = (PickedUpItemTrigger) register("thrown_item_picked_up_by_entity", new PickedUpItemTrigger());
    public static final PickedUpItemTrigger THROWN_ITEM_PICKED_UP_BY_PLAYER = (PickedUpItemTrigger) register("thrown_item_picked_up_by_player", new PickedUpItemTrigger());
    public static final CriterionTriggerPlayerInteractedWithEntity PLAYER_INTERACTED_WITH_ENTITY = (CriterionTriggerPlayerInteractedWithEntity) register("player_interacted_with_entity", new CriterionTriggerPlayerInteractedWithEntity());
    public static final StartRidingTrigger START_RIDING_TRIGGER = (StartRidingTrigger) register("started_riding", new StartRidingTrigger());
    public static final LightningStrikeTrigger LIGHTNING_STRIKE = (LightningStrikeTrigger) register("lightning_strike", new LightningStrikeTrigger());
    public static final UsingItemTrigger USING_ITEM = (UsingItemTrigger) register("using_item", new UsingItemTrigger());
    public static final DistanceTrigger FALL_FROM_HEIGHT = (DistanceTrigger) register("fall_from_height", new DistanceTrigger());
    public static final DistanceTrigger RIDE_ENTITY_IN_LAVA_TRIGGER = (DistanceTrigger) register("ride_entity_in_lava", new DistanceTrigger());
    public static final CriterionTriggerKilled KILL_MOB_NEAR_SCULK_CATALYST = (CriterionTriggerKilled) register("kill_mob_near_sculk_catalyst", new CriterionTriggerKilled());
    public static final ItemUsedOnLocationTrigger ALLAY_DROP_ITEM_ON_BLOCK = (ItemUsedOnLocationTrigger) register("allay_drop_item_on_block", new ItemUsedOnLocationTrigger());
    public static final PlayerTrigger AVOID_VIBRATION = (PlayerTrigger) register("avoid_vibration", new PlayerTrigger());
    public static final RecipeCraftedTrigger RECIPE_CRAFTED = (RecipeCraftedTrigger) register("recipe_crafted", new RecipeCraftedTrigger());
    public static final RecipeCraftedTrigger CRAFTER_RECIPE_CRAFTED = (RecipeCraftedTrigger) register("crafter_recipe_crafted", new RecipeCraftedTrigger());
    public static final FallAfterExplosionTrigger FALL_AFTER_EXPLOSION = (FallAfterExplosionTrigger) register("fall_after_explosion", new FallAfterExplosionTrigger());

    public CriterionTriggers() {}

    private static <T extends CriterionTrigger<?>> T register(String s, T t0) {
        return (CriterionTrigger) IRegistry.register(BuiltInRegistries.TRIGGER_TYPES, s, t0);
    }

    public static CriterionTrigger<?> bootstrap(IRegistry<CriterionTrigger<?>> iregistry) {
        return CriterionTriggers.IMPOSSIBLE;
    }
}
