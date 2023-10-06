package net.minecraft.advancements;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import javax.annotation.Nullable;

public class AdvancementNode {

    private final AdvancementHolder holder;
    @Nullable
    private final AdvancementNode parent;
    private final Set<AdvancementNode> children = new ReferenceOpenHashSet();

    @VisibleForTesting
    public AdvancementNode(AdvancementHolder advancementholder, @Nullable AdvancementNode advancementnode) {
        this.holder = advancementholder;
        this.parent = advancementnode;
    }

    public Advancement advancement() {
        return this.holder.value();
    }

    public AdvancementHolder holder() {
        return this.holder;
    }

    @Nullable
    public AdvancementNode parent() {
        return this.parent;
    }

    public AdvancementNode root() {
        return getRoot(this);
    }

    public static AdvancementNode getRoot(AdvancementNode advancementnode) {
        AdvancementNode advancementnode1 = advancementnode;

        while (true) {
            AdvancementNode advancementnode2 = advancementnode1.parent();

            if (advancementnode2 == null) {
                return advancementnode1;
            }

            advancementnode1 = advancementnode2;
        }
    }

    public Iterable<AdvancementNode> children() {
        return this.children;
    }

    @VisibleForTesting
    public void addChild(AdvancementNode advancementnode) {
        this.children.add(advancementnode);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof AdvancementNode) {
                AdvancementNode advancementnode = (AdvancementNode) object;

                if (this.holder.equals(advancementnode.holder)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.holder.hashCode();
    }

    public String toString() {
        return this.holder.id().toString();
    }
}
