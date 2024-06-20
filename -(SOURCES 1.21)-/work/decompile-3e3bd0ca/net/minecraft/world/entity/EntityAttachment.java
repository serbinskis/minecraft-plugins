package net.minecraft.world.entity;

import java.util.List;
import net.minecraft.world.phys.Vec3D;

public enum EntityAttachment {

    PASSENGER(EntityAttachment.a.AT_HEIGHT), VEHICLE(EntityAttachment.a.AT_FEET), NAME_TAG(EntityAttachment.a.AT_HEIGHT), WARDEN_CHEST(EntityAttachment.a.AT_CENTER);

    private final EntityAttachment.a fallback;

    private EntityAttachment(final EntityAttachment.a entityattachment_a) {
        this.fallback = entityattachment_a;
    }

    public List<Vec3D> createFallbackPoints(float f, float f1) {
        return this.fallback.create(f, f1);
    }

    public interface a {

        List<Vec3D> ZERO = List.of(Vec3D.ZERO);
        EntityAttachment.a AT_FEET = (f, f1) -> {
            return EntityAttachment.a.ZERO;
        };
        EntityAttachment.a AT_HEIGHT = (f, f1) -> {
            return List.of(new Vec3D(0.0D, (double) f1, 0.0D));
        };
        EntityAttachment.a AT_CENTER = (f, f1) -> {
            return List.of(new Vec3D(0.0D, (double) f1 / 2.0D, 0.0D));
        };

        List<Vec3D> create(float f, float f1);
    }
}
