package net.minecraft.world.item.armortrim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public class ArmorTrim implements TooltipProvider {

    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material), TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter((armortrim) -> {
            return armortrim.showInTooltip;
        })).apply(instance, ArmorTrim::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(TrimMaterial.STREAM_CODEC, ArmorTrim::material, TrimPattern.STREAM_CODEC, ArmorTrim::pattern, ByteBufCodecs.BOOL, (armortrim) -> {
        return armortrim.showInTooltip;
    }, ArmorTrim::new);
    private static final IChatBaseComponent UPGRADE_TITLE = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.upgrade"))).withStyle(EnumChatFormat.GRAY);
    private final Holder<TrimMaterial> material;
    private final Holder<TrimPattern> pattern;
    public final boolean showInTooltip;
    private final Function<Holder<ArmorMaterial>, MinecraftKey> innerTexture;
    private final Function<Holder<ArmorMaterial>, MinecraftKey> outerTexture;

    private ArmorTrim(Holder<TrimMaterial> holder, Holder<TrimPattern> holder1, boolean flag, Function<Holder<ArmorMaterial>, MinecraftKey> function, Function<Holder<ArmorMaterial>, MinecraftKey> function1) {
        this.material = holder;
        this.pattern = holder1;
        this.showInTooltip = flag;
        this.innerTexture = function;
        this.outerTexture = function1;
    }

    public ArmorTrim(Holder<TrimMaterial> holder, Holder<TrimPattern> holder1, boolean flag) {
        this.material = holder;
        this.pattern = holder1;
        this.innerTexture = SystemUtils.memoize((holder2) -> {
            MinecraftKey minecraftkey = ((TrimPattern) holder1.value()).assetId();
            String s = getColorPaletteSuffix(holder, holder2);

            return minecraftkey.withPath((s1) -> {
                return "trims/models/armor/" + s1 + "_leggings_" + s;
            });
        });
        this.outerTexture = SystemUtils.memoize((holder2) -> {
            MinecraftKey minecraftkey = ((TrimPattern) holder1.value()).assetId();
            String s = getColorPaletteSuffix(holder, holder2);

            return minecraftkey.withPath((s1) -> {
                return "trims/models/armor/" + s1 + "_" + s;
            });
        });
        this.showInTooltip = flag;
    }

    public ArmorTrim(Holder<TrimMaterial> holder, Holder<TrimPattern> holder1) {
        this(holder, holder1, true);
    }

    private static String getColorPaletteSuffix(Holder<TrimMaterial> holder, Holder<ArmorMaterial> holder1) {
        Map<Holder<ArmorMaterial>, String> map = ((TrimMaterial) holder.value()).overrideArmorMaterials();
        String s = (String) map.get(holder1);

        return s != null ? s : ((TrimMaterial) holder.value()).assetName();
    }

    public boolean hasPatternAndMaterial(Holder<TrimPattern> holder, Holder<TrimMaterial> holder1) {
        return holder.equals(this.pattern) && holder1.equals(this.material);
    }

    public Holder<TrimPattern> pattern() {
        return this.pattern;
    }

    public Holder<TrimMaterial> material() {
        return this.material;
    }

    public MinecraftKey innerTexture(Holder<ArmorMaterial> holder) {
        return (MinecraftKey) this.innerTexture.apply(holder);
    }

    public MinecraftKey outerTexture(Holder<ArmorMaterial> holder) {
        return (MinecraftKey) this.outerTexture.apply(holder);
    }

    public boolean equals(Object object) {
        if (!(object instanceof ArmorTrim armortrim)) {
            return false;
        } else {
            return this.showInTooltip == armortrim.showInTooltip && this.pattern.equals(armortrim.pattern) && this.material.equals(armortrim.material);
        }
    }

    public int hashCode() {
        int i = this.material.hashCode();

        i = 31 * i + this.pattern.hashCode();
        i = 31 * i + (this.showInTooltip ? 1 : 0);
        return i;
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        if (this.showInTooltip) {
            consumer.accept(ArmorTrim.UPGRADE_TITLE);
            consumer.accept(CommonComponents.space().append(((TrimPattern) this.pattern.value()).copyWithStyle(this.material)));
            consumer.accept(CommonComponents.space().append(((TrimMaterial) this.material.value()).description()));
        }
    }

    public ArmorTrim withTooltip(boolean flag) {
        return new ArmorTrim(this.material, this.pattern, flag, this.innerTexture, this.outerTexture);
    }
}
