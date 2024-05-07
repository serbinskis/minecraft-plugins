package net.minecraft.world.entity;

import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public record EntitySize(float width, float height, float eyeHeight, EntityAttachments attachments, boolean fixed) {

    private EntitySize(float f, float f1, boolean flag) {
        this(f, f1, defaultEyeHeight(f1), EntityAttachments.createDefault(f, f1), flag);
    }

    private static float defaultEyeHeight(float f) {
        return f * 0.85F;
    }

    public AxisAlignedBB makeBoundingBox(Vec3D vec3d) {
        return this.makeBoundingBox(vec3d.x, vec3d.y, vec3d.z);
    }

    public AxisAlignedBB makeBoundingBox(double d0, double d1, double d2) {
        float f = this.width / 2.0F;
        float f1 = this.height;

        return new AxisAlignedBB(d0 - (double) f, d1, d2 - (double) f, d0 + (double) f, d1 + (double) f1, d2 + (double) f);
    }

    public EntitySize scale(float f) {
        return this.scale(f, f);
    }

    public EntitySize scale(float f, float f1) {
        return !this.fixed && (f != 1.0F || f1 != 1.0F) ? new EntitySize(this.width * f, this.height * f1, this.eyeHeight * f1, this.attachments.scale(f, f1, f), false) : this;
    }

    public static EntitySize scalable(float f, float f1) {
        return new EntitySize(f, f1, false);
    }

    public static EntitySize fixed(float f, float f1) {
        return new EntitySize(f, f1, true);
    }

    public EntitySize withEyeHeight(float f) {
        return new EntitySize(this.width, this.height, f, this.attachments, this.fixed);
    }

    public EntitySize withAttachments(EntityAttachments.a entityattachments_a) {
        return new EntitySize(this.width, this.height, this.eyeHeight, entityattachments_a.build(this.width, this.height), this.fixed);
    }
}
