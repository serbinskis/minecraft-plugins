package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;

public class LootCollector {

    private final ProblemReporter reporter;
    private final LootContextParameterSet params;
    private final HolderGetter.a resolver;
    private final Set<ResourceKey<?>> visitedElements;

    public LootCollector(ProblemReporter problemreporter, LootContextParameterSet lootcontextparameterset, HolderGetter.a holdergetter_a) {
        this(problemreporter, lootcontextparameterset, holdergetter_a, Set.of());
    }

    private LootCollector(ProblemReporter problemreporter, LootContextParameterSet lootcontextparameterset, HolderGetter.a holdergetter_a, Set<ResourceKey<?>> set) {
        this.reporter = problemreporter;
        this.params = lootcontextparameterset;
        this.resolver = holdergetter_a;
        this.visitedElements = set;
    }

    public LootCollector forChild(String s) {
        return new LootCollector(this.reporter.forChild(s), this.params, this.resolver, this.visitedElements);
    }

    public LootCollector enterElement(String s, ResourceKey<?> resourcekey) {
        Set<ResourceKey<?>> set = ImmutableSet.builder().addAll(this.visitedElements).add(resourcekey).build();

        return new LootCollector(this.reporter.forChild(s), this.params, this.resolver, set);
    }

    public boolean hasVisitedElement(ResourceKey<?> resourcekey) {
        return this.visitedElements.contains(resourcekey);
    }

    public void reportProblem(String s) {
        this.reporter.report(s);
    }

    public void validateUser(LootItemUser lootitemuser) {
        this.params.validateUser(this, lootitemuser);
    }

    public HolderGetter.a resolver() {
        return this.resolver;
    }

    public LootCollector setParams(LootContextParameterSet lootcontextparameterset) {
        return new LootCollector(this.reporter, lootcontextparameterset, this.resolver, this.visitedElements);
    }
}
