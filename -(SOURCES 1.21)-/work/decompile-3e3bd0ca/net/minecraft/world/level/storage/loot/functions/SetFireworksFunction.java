package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetFireworksFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetFireworksFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(ListOperation.e.codec(FireworkExplosion.CODEC, 256).optionalFieldOf("explosions").forGetter((setfireworksfunction) -> {
            return setfireworksfunction.explosions;
        }), ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("flight_duration").forGetter((setfireworksfunction) -> {
            return setfireworksfunction.flightDuration;
        }))).apply(instance, SetFireworksFunction::new);
    });
    public static final Fireworks DEFAULT_VALUE = new Fireworks(0, List.of());
    private final Optional<ListOperation.e<FireworkExplosion>> explosions;
    private final Optional<Integer> flightDuration;

    protected SetFireworksFunction(List<LootItemCondition> list, Optional<ListOperation.e<FireworkExplosion>> optional, Optional<Integer> optional1) {
        super(list);
        this.explosions = optional;
        this.flightDuration = optional1;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.update(DataComponents.FIREWORKS, SetFireworksFunction.DEFAULT_VALUE, this::apply);
        return itemstack;
    }

    private Fireworks apply(Fireworks fireworks) {
        Optional optional = this.flightDuration;

        Objects.requireNonNull(fireworks);
        return new Fireworks((Integer) optional.orElseGet(fireworks::flightDuration), (List) this.explosions.map((listoperation_e) -> {
            return listoperation_e.apply(fireworks.explosions());
        }).orElse(fireworks.explosions()));
    }

    @Override
    public LootItemFunctionType<SetFireworksFunction> getType() {
        return LootItemFunctions.SET_FIREWORKS;
    }
}
