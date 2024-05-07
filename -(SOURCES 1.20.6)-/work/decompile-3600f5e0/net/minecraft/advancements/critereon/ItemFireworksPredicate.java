package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

public record ItemFireworksPredicate(Optional<CollectionPredicate<FireworkExplosion, ItemFireworkExplosionPredicate.a>> explosions, CriterionConditionValue.IntegerRange flightDuration) implements SingleComponentItemPredicate<Fireworks> {

    public static final Codec<ItemFireworksPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(ItemFireworkExplosionPredicate.a.CODEC).optionalFieldOf("explosions").forGetter(ItemFireworksPredicate::explosions), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("flight_duration", CriterionConditionValue.IntegerRange.ANY).forGetter(ItemFireworksPredicate::flightDuration)).apply(instance, ItemFireworksPredicate::new);
    });

    @Override
    public DataComponentType<Fireworks> componentType() {
        return DataComponents.FIREWORKS;
    }

    public boolean matches(ItemStack itemstack, Fireworks fireworks) {
        return this.explosions.isPresent() && !((CollectionPredicate) this.explosions.get()).test((Iterable) fireworks.explosions()) ? false : this.flightDuration.matches(fireworks.flightDuration());
    }
}
