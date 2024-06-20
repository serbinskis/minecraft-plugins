package net.minecraft.world.item.armortrim;

import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimPatterns {

    public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
    public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
    public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
    public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
    public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
    public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
    public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
    public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
    public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
    public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
    public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
    public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
    public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
    public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
    public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
    public static final ResourceKey<TrimPattern> HOST = registryKey("host");
    public static final ResourceKey<TrimPattern> FLOW = registryKey("flow");
    public static final ResourceKey<TrimPattern> BOLT = registryKey("bolt");

    public TrimPatterns() {}

    public static void bootstrap(BootstrapContext<TrimPattern> bootstrapcontext) {
        register(bootstrapcontext, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.SENTRY);
        register(bootstrapcontext, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.DUNE);
        register(bootstrapcontext, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.COAST);
        register(bootstrapcontext, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.WILD);
        register(bootstrapcontext, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.WARD);
        register(bootstrapcontext, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.EYE);
        register(bootstrapcontext, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.VEX);
        register(bootstrapcontext, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.TIDE);
        register(bootstrapcontext, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.SNOUT);
        register(bootstrapcontext, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.RIB);
        register(bootstrapcontext, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.SPIRE);
        register(bootstrapcontext, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.WAYFINDER);
        register(bootstrapcontext, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.SHAPER);
        register(bootstrapcontext, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.SILENCE);
        register(bootstrapcontext, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.RAISER);
        register(bootstrapcontext, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.HOST);
        register(bootstrapcontext, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.FLOW);
        register(bootstrapcontext, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, TrimPatterns.BOLT);
    }

    public static Optional<Holder.c<TrimPattern>> getFromTemplate(HolderLookup.a holderlookup_a, ItemStack itemstack) {
        return holderlookup_a.lookupOrThrow(Registries.TRIM_PATTERN).listElements().filter((holder_c) -> {
            return itemstack.is(((TrimPattern) holder_c.value()).templateItem());
        }).findFirst();
    }

    public static void register(BootstrapContext<TrimPattern> bootstrapcontext, Item item, ResourceKey<TrimPattern> resourcekey) {
        TrimPattern trimpattern = new TrimPattern(resourcekey.location(), BuiltInRegistries.ITEM.wrapAsHolder(item), IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("trim_pattern", resourcekey.location())), false);

        bootstrapcontext.register(resourcekey, trimpattern);
    }

    private static ResourceKey<TrimPattern> registryKey(String s) {
        return ResourceKey.create(Registries.TRIM_PATTERN, MinecraftKey.withDefaultNamespace(s));
    }
}
