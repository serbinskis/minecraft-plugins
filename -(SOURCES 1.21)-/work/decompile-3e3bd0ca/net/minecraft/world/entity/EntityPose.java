package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum EntityPose {

    STANDING(0), FALL_FLYING(1), SLEEPING(2), SWIMMING(3), SPIN_ATTACK(4), CROUCHING(5), LONG_JUMPING(6), DYING(7), CROAKING(8), USING_TONGUE(9), SITTING(10), ROARING(11), SNIFFING(12), EMERGING(13), DIGGING(14), SLIDING(15), SHOOTING(16), INHALING(17);

    public static final IntFunction<EntityPose> BY_ID = ByIdMap.continuous(EntityPose::id, values(), ByIdMap.a.ZERO);
    public static final StreamCodec<ByteBuf, EntityPose> STREAM_CODEC = ByteBufCodecs.idMapper(EntityPose.BY_ID, EntityPose::id);
    private final int id;

    private EntityPose(final int i) {
        this.id = i;
    }

    public int id() {
        return this.id;
    }
}
