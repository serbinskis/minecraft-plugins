package net.minecraft.world.level.gameevent;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.phys.Vec3D;

public interface GameEventListener {

    PositionSource getListenerSource();

    int getListenerRadius();

    boolean handleGameEvent(WorldServer worldserver, Holder<GameEvent> holder, GameEvent.a gameevent_a, Vec3D vec3d);

    default GameEventListener.a getDeliveryMode() {
        return GameEventListener.a.UNSPECIFIED;
    }

    public static enum a {

        UNSPECIFIED, BY_DISTANCE;

        private a() {}
    }

    public interface b<T extends GameEventListener> {

        T getListener();
    }
}
