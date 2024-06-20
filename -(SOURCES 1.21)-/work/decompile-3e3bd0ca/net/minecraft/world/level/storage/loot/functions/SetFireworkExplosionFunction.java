package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetFireworkExplosionFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetFireworkExplosionFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(FireworkExplosion.a.CODEC.optionalFieldOf("shape").forGetter((setfireworkexplosionfunction) -> {
            return setfireworkexplosionfunction.shape;
        }), FireworkExplosion.COLOR_LIST_CODEC.optionalFieldOf("colors").forGetter((setfireworkexplosionfunction) -> {
            return setfireworkexplosionfunction.colors;
        }), FireworkExplosion.COLOR_LIST_CODEC.optionalFieldOf("fade_colors").forGetter((setfireworkexplosionfunction) -> {
            return setfireworkexplosionfunction.fadeColors;
        }), Codec.BOOL.optionalFieldOf("trail").forGetter((setfireworkexplosionfunction) -> {
            return setfireworkexplosionfunction.trail;
        }), Codec.BOOL.optionalFieldOf("twinkle").forGetter((setfireworkexplosionfunction) -> {
            return setfireworkexplosionfunction.twinkle;
        }))).apply(instance, SetFireworkExplosionFunction::new);
    });
    public static final FireworkExplosion DEFAULT_VALUE = new FireworkExplosion(FireworkExplosion.a.SMALL_BALL, IntList.of(), IntList.of(), false, false);
    final Optional<FireworkExplosion.a> shape;
    final Optional<IntList> colors;
    final Optional<IntList> fadeColors;
    final Optional<Boolean> trail;
    final Optional<Boolean> twinkle;

    public SetFireworkExplosionFunction(List<LootItemCondition> list, Optional<FireworkExplosion.a> optional, Optional<IntList> optional1, Optional<IntList> optional2, Optional<Boolean> optional3, Optional<Boolean> optional4) {
        super(list);
        this.shape = optional;
        this.colors = optional1;
        this.fadeColors = optional2;
        this.trail = optional3;
        this.twinkle = optional4;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.update(DataComponents.FIREWORK_EXPLOSION, SetFireworkExplosionFunction.DEFAULT_VALUE, this::apply);
        return itemstack;
    }

    private FireworkExplosion apply(FireworkExplosion fireworkexplosion) {
        Optional optional = this.shape;

        Objects.requireNonNull(fireworkexplosion);
        FireworkExplosion.a fireworkexplosion_a = (FireworkExplosion.a) optional.orElseGet(fireworkexplosion::shape);
        Optional optional1 = this.colors;

        Objects.requireNonNull(fireworkexplosion);
        IntList intlist = (IntList) optional1.orElseGet(fireworkexplosion::colors);
        Optional optional2 = this.fadeColors;

        Objects.requireNonNull(fireworkexplosion);
        IntList intlist1 = (IntList) optional2.orElseGet(fireworkexplosion::fadeColors);
        Optional optional3 = this.trail;

        Objects.requireNonNull(fireworkexplosion);
        boolean flag = (Boolean) optional3.orElseGet(fireworkexplosion::hasTrail);
        Optional optional4 = this.twinkle;

        Objects.requireNonNull(fireworkexplosion);
        return new FireworkExplosion(fireworkexplosion_a, intlist, intlist1, flag, (Boolean) optional4.orElseGet(fireworkexplosion::hasTwinkle));
    }

    @Override
    public LootItemFunctionType<SetFireworkExplosionFunction> getType() {
        return LootItemFunctions.SET_FIREWORK_EXPLOSION;
    }
}
