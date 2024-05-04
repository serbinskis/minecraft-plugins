package net.minecraft.world.entity.ai.village.poi;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.IBlockData;

public record VillagePlaceType(Set<IBlockData> matchingStates, int maxTickets, int validRange) {

    public static final Predicate<Holder<VillagePlaceType>> NONE = (holder) -> {
        return false;
    };

    public VillagePlaceType(Set<IBlockData> matchingStates, int maxTickets, int validRange) {
        matchingStates = Set.copyOf(matchingStates);
        this.matchingStates = matchingStates;
        this.maxTickets = maxTickets;
        this.validRange = validRange;
    }

    public boolean is(IBlockData iblockdata) {
        return this.matchingStates.contains(iblockdata);
    }
}
