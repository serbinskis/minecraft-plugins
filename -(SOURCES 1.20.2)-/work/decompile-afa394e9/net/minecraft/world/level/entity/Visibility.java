package net.minecraft.world.level.entity;

import net.minecraft.server.level.FullChunkStatus;

public enum Visibility {

    HIDDEN(false, false), TRACKED(true, false), TICKING(true, true);

    private final boolean accessible;
    private final boolean ticking;

    private Visibility(boolean flag, boolean flag1) {
        this.accessible = flag;
        this.ticking = flag1;
    }

    public boolean isTicking() {
        return this.ticking;
    }

    public boolean isAccessible() {
        return this.accessible;
    }

    public static Visibility fromFullChunkStatus(FullChunkStatus fullchunkstatus) {
        return fullchunkstatus.isOrAfter(FullChunkStatus.ENTITY_TICKING) ? Visibility.TICKING : (fullchunkstatus.isOrAfter(FullChunkStatus.FULL) ? Visibility.TRACKED : Visibility.HIDDEN);
    }
}
