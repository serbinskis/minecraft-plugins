package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.raid.EntityRaider;
import net.minecraft.world.phys.Vec3D;

public record RaiderPredicate(boolean hasRaid, boolean isCaptain) implements EntitySubPredicate {

    public static final MapCodec<RaiderPredicate> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.BOOL.optionalFieldOf("has_raid", false).forGetter(RaiderPredicate::hasRaid), Codec.BOOL.optionalFieldOf("is_captain", false).forGetter(RaiderPredicate::isCaptain)).apply(instance, RaiderPredicate::new);
    });
    public static final RaiderPredicate CAPTAIN_WITHOUT_RAID = new RaiderPredicate(false, true);

    @Override
    public MapCodec<RaiderPredicate> codec() {
        return EntitySubPredicates.RAIDER;
    }

    @Override
    public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
        if (!(entity instanceof EntityRaider entityraider)) {
            return false;
        } else {
            return entityraider.hasRaid() == this.hasRaid && entityraider.isCaptain() == this.isCaptain;
        }
    }
}
