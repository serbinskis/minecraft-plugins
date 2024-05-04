package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.EncoderCache;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.ChestLock;
import net.minecraft.world.food.FoodInfo;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.EnumItemRarity;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.entity.TileEntityBeehive;
import net.minecraft.world.level.saveddata.maps.MapId;

public class DataComponents {

    static final EncoderCache ENCODER_CACHE = new EncoderCache(512);
    public static final DataComponentType<CustomData> CUSTOM_DATA = register("custom_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomData.CODEC);
    });
    public static final DataComponentType<Integer> MAX_STACK_SIZE = register("max_stack_size", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.intRange(1, 99)).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<Integer> MAX_DAMAGE = register("max_damage", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<Integer> DAMAGE = register("damage", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<Unbreakable> UNBREAKABLE = register("unbreakable", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Unbreakable.CODEC).networkSynchronized(Unbreakable.STREAM_CODEC);
    });
    public static final DataComponentType<IChatBaseComponent> CUSTOM_NAME = register("custom_name", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<IChatBaseComponent> ITEM_NAME = register("item_name", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ItemLore> LORE = register("lore", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemLore.CODEC).networkSynchronized(ItemLore.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<EnumItemRarity> RARITY = register("rarity", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumItemRarity.CODEC).networkSynchronized(EnumItemRarity.STREAM_CODEC);
    });
    public static final DataComponentType<ItemEnchantments> ENCHANTMENTS = register("enchantments", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<AdventureModePredicate> CAN_PLACE_ON = register("can_place_on", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<AdventureModePredicate> CAN_BREAK = register("can_break", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ItemAttributeModifiers> ATTRIBUTE_MODIFIERS = register("attribute_modifiers", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemAttributeModifiers.CODEC).networkSynchronized(ItemAttributeModifiers.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<CustomModelData> CUSTOM_MODEL_DATA = register("custom_model_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomModelData.CODEC).networkSynchronized(CustomModelData.STREAM_CODEC);
    });
    public static final DataComponentType<Unit> HIDE_ADDITIONAL_TOOLTIP = register("hide_additional_tooltip", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE));
    });
    public static final DataComponentType<Unit> HIDE_TOOLTIP = register("hide_tooltip", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE));
    });
    public static final DataComponentType<Integer> REPAIR_COST = register("repair_cost", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<Unit> CREATIVE_SLOT_LOCK = register("creative_slot_lock", (datacomponenttype_a) -> {
        return datacomponenttype_a.networkSynchronized(StreamCodec.unit(Unit.INSTANCE));
    });
    public static final DataComponentType<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register("enchantment_glint_override", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL);
    });
    public static final DataComponentType<Unit> INTANGIBLE_PROJECTILE = register("intangible_projectile", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Codec.unit(Unit.INSTANCE));
    });
    public static final DataComponentType<FoodInfo> FOOD = register("food", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(FoodInfo.DIRECT_CODEC).networkSynchronized(FoodInfo.DIRECT_STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Unit> FIRE_RESISTANT = register("fire_resistant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE));
    });
    public static final DataComponentType<Tool> TOOL = register("tool", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Tool.CODEC).networkSynchronized(Tool.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ItemEnchantments> STORED_ENCHANTMENTS = register("stored_enchantments", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<DyedItemColor> DYED_COLOR = register("dyed_color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(DyedItemColor.CODEC).networkSynchronized(DyedItemColor.STREAM_CODEC);
    });
    public static final DataComponentType<MapItemColor> MAP_COLOR = register("map_color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MapItemColor.CODEC).networkSynchronized(MapItemColor.STREAM_CODEC);
    });
    public static final DataComponentType<MapId> MAP_ID = register("map_id", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MapId.CODEC).networkSynchronized(MapId.STREAM_CODEC);
    });
    public static final DataComponentType<MapDecorations> MAP_DECORATIONS = register("map_decorations", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MapDecorations.CODEC).cacheEncoding();
    });
    public static final DataComponentType<MapPostProcessing> MAP_POST_PROCESSING = register("map_post_processing", (datacomponenttype_a) -> {
        return datacomponenttype_a.networkSynchronized(MapPostProcessing.STREAM_CODEC);
    });
    public static final DataComponentType<ChargedProjectiles> CHARGED_PROJECTILES = register("charged_projectiles", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ChargedProjectiles.CODEC).networkSynchronized(ChargedProjectiles.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<BundleContents> BUNDLE_CONTENTS = register("bundle_contents", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(BundleContents.CODEC).networkSynchronized(BundleContents.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<PotionContents> POTION_CONTENTS = register("potion_contents", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(PotionContents.CODEC).networkSynchronized(PotionContents.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = register("suspicious_stew_effects", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(SuspiciousStewEffects.CODEC).networkSynchronized(SuspiciousStewEffects.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<WritableBookContent> WRITABLE_BOOK_CONTENT = register("writable_book_content", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(WritableBookContent.CODEC).networkSynchronized(WritableBookContent.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<WrittenBookContent> WRITTEN_BOOK_CONTENT = register("written_book_content", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(WrittenBookContent.CODEC).networkSynchronized(WrittenBookContent.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ArmorTrim> TRIM = register("trim", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ArmorTrim.CODEC).networkSynchronized(ArmorTrim.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<DebugStickState> DEBUG_STICK_STATE = register("debug_stick_state", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(DebugStickState.CODEC).cacheEncoding();
    });
    public static final DataComponentType<CustomData> ENTITY_DATA = register("entity_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC);
    });
    public static final DataComponentType<CustomData> BUCKET_ENTITY_DATA = register("bucket_entity_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC);
    });
    public static final DataComponentType<CustomData> BLOCK_ENTITY_DATA = register("block_entity_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC);
    });
    public static final DataComponentType<Holder<Instrument>> INSTRUMENT = register("instrument", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Instrument.CODEC).networkSynchronized(Instrument.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Integer> OMINOUS_BOTTLE_AMPLIFIER = register("ominous_bottle_amplifier", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.intRange(0, 4)).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<List<MinecraftKey>> RECIPES = register("recipes", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MinecraftKey.CODEC.listOf()).cacheEncoding();
    });
    public static final DataComponentType<LodestoneTracker> LODESTONE_TRACKER = register("lodestone_tracker", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(LodestoneTracker.CODEC).networkSynchronized(LodestoneTracker.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<FireworkExplosion> FIREWORK_EXPLOSION = register("firework_explosion", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(FireworkExplosion.CODEC).networkSynchronized(FireworkExplosion.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Fireworks> FIREWORKS = register("fireworks", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Fireworks.CODEC).networkSynchronized(Fireworks.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ResolvableProfile> PROFILE = register("profile", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ResolvableProfile.CODEC).networkSynchronized(ResolvableProfile.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<MinecraftKey> NOTE_BLOCK_SOUND = register("note_block_sound", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MinecraftKey.CODEC).networkSynchronized(MinecraftKey.STREAM_CODEC);
    });
    public static final DataComponentType<BannerPatternLayers> BANNER_PATTERNS = register("banner_patterns", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(BannerPatternLayers.CODEC).networkSynchronized(BannerPatternLayers.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<EnumColor> BASE_COLOR = register("base_color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumColor.CODEC).networkSynchronized(EnumColor.STREAM_CODEC);
    });
    public static final DataComponentType<PotDecorations> POT_DECORATIONS = register("pot_decorations", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(PotDecorations.CODEC).networkSynchronized(PotDecorations.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ItemContainerContents> CONTAINER = register("container", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<BlockItemStateProperties> BLOCK_STATE = register("block_state", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(BlockItemStateProperties.CODEC).networkSynchronized(BlockItemStateProperties.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<List<TileEntityBeehive.c>> BEES = register("bees", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(TileEntityBeehive.c.LIST_CODEC).networkSynchronized(TileEntityBeehive.c.STREAM_CODEC.apply(ByteBufCodecs.list())).cacheEncoding();
    });
    public static final DataComponentType<ChestLock> LOCK = register("lock", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ChestLock.CODEC);
    });
    public static final DataComponentType<SeededContainerLoot> CONTAINER_LOOT = register("container_loot", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(SeededContainerLoot.CODEC);
    });
    public static final DataComponentMap COMMON_ITEM_COMPONENTS = DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).set(DataComponents.LORE, ItemLore.EMPTY).set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).set(DataComponents.REPAIR_COST, 0).set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).set(DataComponents.RARITY, EnumItemRarity.COMMON).build();

    public DataComponents() {}

    public static DataComponentType<?> bootstrap(IRegistry<DataComponentType<?>> iregistry) {
        return DataComponents.CUSTOM_DATA;
    }

    private static <T> DataComponentType<T> register(String s, UnaryOperator<DataComponentType.a<T>> unaryoperator) {
        return (DataComponentType) IRegistry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, s, ((DataComponentType.a) unaryoperator.apply(DataComponentType.builder())).build());
    }
}
