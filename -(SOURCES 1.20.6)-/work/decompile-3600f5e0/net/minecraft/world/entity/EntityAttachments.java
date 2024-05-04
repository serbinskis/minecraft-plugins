package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.phys.Vec3D;

public class EntityAttachments {

    private final Map<EntityAttachment, List<Vec3D>> attachments;

    EntityAttachments(Map<EntityAttachment, List<Vec3D>> map) {
        this.attachments = map;
    }

    public static EntityAttachments createDefault(float f, float f1) {
        return builder().build(f, f1);
    }

    public static EntityAttachments.a builder() {
        return new EntityAttachments.a();
    }

    public EntityAttachments scale(float f, float f1, float f2) {
        Map<EntityAttachment, List<Vec3D>> map = new EnumMap(EntityAttachment.class);
        Iterator iterator = this.attachments.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<EntityAttachment, List<Vec3D>> entry = (Entry) iterator.next();

            map.put((EntityAttachment) entry.getKey(), scalePoints((List) entry.getValue(), f, f1, f2));
        }

        return new EntityAttachments(map);
    }

    private static List<Vec3D> scalePoints(List<Vec3D> list, float f, float f1, float f2) {
        List<Vec3D> list1 = new ArrayList(list.size());
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Vec3D vec3d = (Vec3D) iterator.next();

            list1.add(vec3d.multiply((double) f, (double) f1, (double) f2));
        }

        return list1;
    }

    @Nullable
    public Vec3D getNullable(EntityAttachment entityattachment, int i, float f) {
        List<Vec3D> list = (List) this.attachments.get(entityattachment);

        return i >= 0 && i < list.size() ? transformPoint((Vec3D) list.get(i), f) : null;
    }

    public Vec3D get(EntityAttachment entityattachment, int i, float f) {
        Vec3D vec3d = this.getNullable(entityattachment, i, f);

        if (vec3d == null) {
            String s = String.valueOf(entityattachment);

            throw new IllegalStateException("Had no attachment point of type: " + s + " for index: " + i);
        } else {
            return vec3d;
        }
    }

    public Vec3D getClamped(EntityAttachment entityattachment, int i, float f) {
        List<Vec3D> list = (List) this.attachments.get(entityattachment);

        if (list.isEmpty()) {
            throw new IllegalStateException("Had no attachment points of type: " + String.valueOf(entityattachment));
        } else {
            Vec3D vec3d = (Vec3D) list.get(MathHelper.clamp(i, 0, list.size() - 1));

            return transformPoint(vec3d, f);
        }
    }

    private static Vec3D transformPoint(Vec3D vec3d, float f) {
        return vec3d.yRot(-f * 0.017453292F);
    }

    public static class a {

        private final Map<EntityAttachment, List<Vec3D>> attachments = new EnumMap(EntityAttachment.class);

        a() {}

        public EntityAttachments.a attach(EntityAttachment entityattachment, float f, float f1, float f2) {
            return this.attach(entityattachment, new Vec3D((double) f, (double) f1, (double) f2));
        }

        public EntityAttachments.a attach(EntityAttachment entityattachment, Vec3D vec3d) {
            ((List) this.attachments.computeIfAbsent(entityattachment, (entityattachment1) -> {
                return new ArrayList(1);
            })).add(vec3d);
            return this;
        }

        public EntityAttachments build(float f, float f1) {
            Map<EntityAttachment, List<Vec3D>> map = new EnumMap(EntityAttachment.class);
            EntityAttachment[] aentityattachment = EntityAttachment.values();
            int i = aentityattachment.length;

            for (int j = 0; j < i; ++j) {
                EntityAttachment entityattachment = aentityattachment[j];
                List<Vec3D> list = (List) this.attachments.get(entityattachment);

                map.put(entityattachment, list != null ? List.copyOf(list) : entityattachment.createFallbackPoints(f, f1));
            }

            return new EntityAttachments(map);
        }
    }
}
