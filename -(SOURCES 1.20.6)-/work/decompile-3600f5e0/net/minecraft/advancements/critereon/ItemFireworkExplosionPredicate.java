package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;

public record ItemFireworkExplosionPredicate(ItemFireworkExplosionPredicate.a predicate) implements SingleComponentItemPredicate<FireworkExplosion> {

    public static final Codec<ItemFireworkExplosionPredicate> CODEC = ItemFireworkExplosionPredicate.a.CODEC.xmap(ItemFireworkExplosionPredicate::new, ItemFireworkExplosionPredicate::predicate);

    @Override
    public DataComponentType<FireworkExplosion> componentType() {
        return DataComponents.FIREWORK_EXPLOSION;
    }

    public boolean matches(ItemStack itemstack, FireworkExplosion fireworkexplosion) {
        return this.predicate.test(fireworkexplosion);
    }

    public static record a(Optional<FireworkExplosion.a> shape, Optional<Boolean> twinkle, Optional<Boolean> trail) implements Predicate<FireworkExplosion> {

        public static final Codec<ItemFireworkExplosionPredicate.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(FireworkExplosion.a.CODEC.optionalFieldOf("shape").forGetter(ItemFireworkExplosionPredicate.a::shape), Codec.BOOL.optionalFieldOf("has_twinkle").forGetter(ItemFireworkExplosionPredicate.a::twinkle), Codec.BOOL.optionalFieldOf("has_trail").forGetter(ItemFireworkExplosionPredicate.a::trail)).apply(instance, ItemFireworkExplosionPredicate.a::new);
        });

        public boolean test(FireworkExplosion fireworkexplosion) {
            return this.shape.isPresent() && this.shape.get() != fireworkexplosion.shape() ? false : (this.twinkle.isPresent() && (Boolean) this.twinkle.get() != fireworkexplosion.hasTwinkle() ? false : !this.trail.isPresent() || (Boolean) this.trail.get() == fireworkexplosion.hasTrail());
        }
    }
}
