package net.minecraft.world.flag;

public class FeatureFlag {

    public final FeatureFlagUniverse universe;
    public final long mask;

    FeatureFlag(FeatureFlagUniverse featureflaguniverse, int i) {
        this.universe = featureflaguniverse;
        this.mask = 1L << i;
    }
}
