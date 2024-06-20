package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;

public interface Portal {

    default int getPortalTransitionTime(WorldServer worldserver, Entity entity) {
        return 0;
    }

    @Nullable
    DimensionTransition getPortalDestination(WorldServer worldserver, Entity entity, BlockPosition blockposition);

    default Portal.a getLocalTransition() {
        return Portal.a.NONE;
    }

    public static enum a {

        CONFUSION, NONE;

        private a() {}
    }
}
