package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.DimensionTransition;

public class PortalProcessor {

    private Portal portal;
    private BlockPosition entryPosition;
    private int portalTime;
    private boolean insidePortalThisTick;

    public PortalProcessor(Portal portal, BlockPosition blockposition) {
        this.portal = portal;
        this.entryPosition = blockposition;
        this.insidePortalThisTick = true;
    }

    public boolean processPortalTeleportation(WorldServer worldserver, Entity entity, boolean flag) {
        if (!this.insidePortalThisTick) {
            this.decayTick();
            return false;
        } else {
            this.insidePortalThisTick = false;
            return flag && this.portalTime++ >= this.portal.getPortalTransitionTime(worldserver, entity);
        }
    }

    @Nullable
    public DimensionTransition getPortalDestination(WorldServer worldserver, Entity entity) {
        return this.portal.getPortalDestination(worldserver, entity, this.entryPosition);
    }

    public Portal.a getPortalLocalTransition() {
        return this.portal.getLocalTransition();
    }

    private void decayTick() {
        this.portalTime = Math.max(this.portalTime - 4, 0);
    }

    public boolean hasExpired() {
        return this.portalTime <= 0;
    }

    public BlockPosition getEntryPosition() {
        return this.entryPosition;
    }

    public void updateEntryPosition(BlockPosition blockposition) {
        this.entryPosition = blockposition;
    }

    public int getPortalTime() {
        return this.portalTime;
    }

    public boolean isInsidePortalThisTick() {
        return this.insidePortalThisTick;
    }

    public void setAsInsidePortalThisTick(boolean flag) {
        this.insidePortalThisTick = flag;
    }

    public boolean isSamePortal(Portal portal) {
        return this.portal == portal;
    }
}
