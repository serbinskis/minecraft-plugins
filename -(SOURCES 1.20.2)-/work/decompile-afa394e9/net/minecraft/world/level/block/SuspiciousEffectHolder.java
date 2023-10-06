package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.level.IMaterial;

public interface SuspiciousEffectHolder {

    List<SuspiciousEffectHolder.a> getSuspiciousEffects();

    static List<SuspiciousEffectHolder> getAllEffectHolders() {
        return (List) BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    static SuspiciousEffectHolder tryGet(IMaterial imaterial) {
        Item item = imaterial.asItem();

        if (item instanceof ItemBlock) {
            ItemBlock itemblock = (ItemBlock) item;
            Block block = itemblock.getBlock();

            if (block instanceof SuspiciousEffectHolder) {
                SuspiciousEffectHolder suspiciouseffectholder = (SuspiciousEffectHolder) block;

                return suspiciouseffectholder;
            }
        }

        Item item1 = imaterial.asItem();

        if (item1 instanceof SuspiciousEffectHolder) {
            SuspiciousEffectHolder suspiciouseffectholder1 = (SuspiciousEffectHolder) item1;

            return suspiciouseffectholder1;
        } else {
            return null;
        }
    }

    public static record a(MobEffectList effect, int duration) {

        public static final Codec<SuspiciousEffectHolder.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("id").forGetter(SuspiciousEffectHolder.a::effect), Codec.INT.optionalFieldOf("duration", 160).forGetter(SuspiciousEffectHolder.a::duration)).apply(instance, SuspiciousEffectHolder.a::new);
        });
        public static final Codec<List<SuspiciousEffectHolder.a>> LIST_CODEC = SuspiciousEffectHolder.a.CODEC.listOf();

        public MobEffect createEffectInstance() {
            return new MobEffect(this.effect, this.duration);
        }
    }
}
