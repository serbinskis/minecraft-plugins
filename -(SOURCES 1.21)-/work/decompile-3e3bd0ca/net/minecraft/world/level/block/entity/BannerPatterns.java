package net.minecraft.world.level.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public class BannerPatterns {

    public static final ResourceKey<EnumBannerPatternType> BASE = create("base");
    public static final ResourceKey<EnumBannerPatternType> SQUARE_BOTTOM_LEFT = create("square_bottom_left");
    public static final ResourceKey<EnumBannerPatternType> SQUARE_BOTTOM_RIGHT = create("square_bottom_right");
    public static final ResourceKey<EnumBannerPatternType> SQUARE_TOP_LEFT = create("square_top_left");
    public static final ResourceKey<EnumBannerPatternType> SQUARE_TOP_RIGHT = create("square_top_right");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_BOTTOM = create("stripe_bottom");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_TOP = create("stripe_top");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_LEFT = create("stripe_left");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_RIGHT = create("stripe_right");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_CENTER = create("stripe_center");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_MIDDLE = create("stripe_middle");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_DOWNRIGHT = create("stripe_downright");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_DOWNLEFT = create("stripe_downleft");
    public static final ResourceKey<EnumBannerPatternType> STRIPE_SMALL = create("small_stripes");
    public static final ResourceKey<EnumBannerPatternType> CROSS = create("cross");
    public static final ResourceKey<EnumBannerPatternType> STRAIGHT_CROSS = create("straight_cross");
    public static final ResourceKey<EnumBannerPatternType> TRIANGLE_BOTTOM = create("triangle_bottom");
    public static final ResourceKey<EnumBannerPatternType> TRIANGLE_TOP = create("triangle_top");
    public static final ResourceKey<EnumBannerPatternType> TRIANGLES_BOTTOM = create("triangles_bottom");
    public static final ResourceKey<EnumBannerPatternType> TRIANGLES_TOP = create("triangles_top");
    public static final ResourceKey<EnumBannerPatternType> DIAGONAL_LEFT = create("diagonal_left");
    public static final ResourceKey<EnumBannerPatternType> DIAGONAL_RIGHT = create("diagonal_up_right");
    public static final ResourceKey<EnumBannerPatternType> DIAGONAL_LEFT_MIRROR = create("diagonal_up_left");
    public static final ResourceKey<EnumBannerPatternType> DIAGONAL_RIGHT_MIRROR = create("diagonal_right");
    public static final ResourceKey<EnumBannerPatternType> CIRCLE_MIDDLE = create("circle");
    public static final ResourceKey<EnumBannerPatternType> RHOMBUS_MIDDLE = create("rhombus");
    public static final ResourceKey<EnumBannerPatternType> HALF_VERTICAL = create("half_vertical");
    public static final ResourceKey<EnumBannerPatternType> HALF_HORIZONTAL = create("half_horizontal");
    public static final ResourceKey<EnumBannerPatternType> HALF_VERTICAL_MIRROR = create("half_vertical_right");
    public static final ResourceKey<EnumBannerPatternType> HALF_HORIZONTAL_MIRROR = create("half_horizontal_bottom");
    public static final ResourceKey<EnumBannerPatternType> BORDER = create("border");
    public static final ResourceKey<EnumBannerPatternType> CURLY_BORDER = create("curly_border");
    public static final ResourceKey<EnumBannerPatternType> GRADIENT = create("gradient");
    public static final ResourceKey<EnumBannerPatternType> GRADIENT_UP = create("gradient_up");
    public static final ResourceKey<EnumBannerPatternType> BRICKS = create("bricks");
    public static final ResourceKey<EnumBannerPatternType> GLOBE = create("globe");
    public static final ResourceKey<EnumBannerPatternType> CREEPER = create("creeper");
    public static final ResourceKey<EnumBannerPatternType> SKULL = create("skull");
    public static final ResourceKey<EnumBannerPatternType> FLOWER = create("flower");
    public static final ResourceKey<EnumBannerPatternType> MOJANG = create("mojang");
    public static final ResourceKey<EnumBannerPatternType> PIGLIN = create("piglin");
    public static final ResourceKey<EnumBannerPatternType> FLOW = create("flow");
    public static final ResourceKey<EnumBannerPatternType> GUSTER = create("guster");

    public BannerPatterns() {}

    private static ResourceKey<EnumBannerPatternType> create(String s) {
        return ResourceKey.create(Registries.BANNER_PATTERN, MinecraftKey.withDefaultNamespace(s));
    }

    public static void bootstrap(BootstrapContext<EnumBannerPatternType> bootstrapcontext) {
        register(bootstrapcontext, BannerPatterns.BASE);
        register(bootstrapcontext, BannerPatterns.SQUARE_BOTTOM_LEFT);
        register(bootstrapcontext, BannerPatterns.SQUARE_BOTTOM_RIGHT);
        register(bootstrapcontext, BannerPatterns.SQUARE_TOP_LEFT);
        register(bootstrapcontext, BannerPatterns.SQUARE_TOP_RIGHT);
        register(bootstrapcontext, BannerPatterns.STRIPE_BOTTOM);
        register(bootstrapcontext, BannerPatterns.STRIPE_TOP);
        register(bootstrapcontext, BannerPatterns.STRIPE_LEFT);
        register(bootstrapcontext, BannerPatterns.STRIPE_RIGHT);
        register(bootstrapcontext, BannerPatterns.STRIPE_CENTER);
        register(bootstrapcontext, BannerPatterns.STRIPE_MIDDLE);
        register(bootstrapcontext, BannerPatterns.STRIPE_DOWNRIGHT);
        register(bootstrapcontext, BannerPatterns.STRIPE_DOWNLEFT);
        register(bootstrapcontext, BannerPatterns.STRIPE_SMALL);
        register(bootstrapcontext, BannerPatterns.CROSS);
        register(bootstrapcontext, BannerPatterns.STRAIGHT_CROSS);
        register(bootstrapcontext, BannerPatterns.TRIANGLE_BOTTOM);
        register(bootstrapcontext, BannerPatterns.TRIANGLE_TOP);
        register(bootstrapcontext, BannerPatterns.TRIANGLES_BOTTOM);
        register(bootstrapcontext, BannerPatterns.TRIANGLES_TOP);
        register(bootstrapcontext, BannerPatterns.DIAGONAL_LEFT);
        register(bootstrapcontext, BannerPatterns.DIAGONAL_RIGHT);
        register(bootstrapcontext, BannerPatterns.DIAGONAL_LEFT_MIRROR);
        register(bootstrapcontext, BannerPatterns.DIAGONAL_RIGHT_MIRROR);
        register(bootstrapcontext, BannerPatterns.CIRCLE_MIDDLE);
        register(bootstrapcontext, BannerPatterns.RHOMBUS_MIDDLE);
        register(bootstrapcontext, BannerPatterns.HALF_VERTICAL);
        register(bootstrapcontext, BannerPatterns.HALF_HORIZONTAL);
        register(bootstrapcontext, BannerPatterns.HALF_VERTICAL_MIRROR);
        register(bootstrapcontext, BannerPatterns.HALF_HORIZONTAL_MIRROR);
        register(bootstrapcontext, BannerPatterns.BORDER);
        register(bootstrapcontext, BannerPatterns.CURLY_BORDER);
        register(bootstrapcontext, BannerPatterns.GRADIENT);
        register(bootstrapcontext, BannerPatterns.GRADIENT_UP);
        register(bootstrapcontext, BannerPatterns.BRICKS);
        register(bootstrapcontext, BannerPatterns.GLOBE);
        register(bootstrapcontext, BannerPatterns.CREEPER);
        register(bootstrapcontext, BannerPatterns.SKULL);
        register(bootstrapcontext, BannerPatterns.FLOWER);
        register(bootstrapcontext, BannerPatterns.MOJANG);
        register(bootstrapcontext, BannerPatterns.PIGLIN);
        register(bootstrapcontext, BannerPatterns.FLOW);
        register(bootstrapcontext, BannerPatterns.GUSTER);
    }

    public static void register(BootstrapContext<EnumBannerPatternType> bootstrapcontext, ResourceKey<EnumBannerPatternType> resourcekey) {
        bootstrapcontext.register(resourcekey, new EnumBannerPatternType(resourcekey.location(), "block.minecraft.banner." + resourcekey.location().toShortLanguageKey()));
    }
}
