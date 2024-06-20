package net.minecraft.advancements;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import org.slf4j.Logger;

public class AdvancementTree {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<MinecraftKey, AdvancementNode> nodes = new Object2ObjectOpenHashMap();
    private final Set<AdvancementNode> roots = new ObjectLinkedOpenHashSet();
    private final Set<AdvancementNode> tasks = new ObjectLinkedOpenHashSet();
    @Nullable
    private AdvancementTree.a listener;

    public AdvancementTree() {}

    private void remove(AdvancementNode advancementnode) {
        Iterator iterator = advancementnode.children().iterator();

        while (iterator.hasNext()) {
            AdvancementNode advancementnode1 = (AdvancementNode) iterator.next();

            this.remove(advancementnode1);
        }

        AdvancementTree.LOGGER.info("Forgot about advancement {}", advancementnode.holder());
        this.nodes.remove(advancementnode.holder().id());
        if (advancementnode.parent() == null) {
            this.roots.remove(advancementnode);
            if (this.listener != null) {
                this.listener.onRemoveAdvancementRoot(advancementnode);
            }
        } else {
            this.tasks.remove(advancementnode);
            if (this.listener != null) {
                this.listener.onRemoveAdvancementTask(advancementnode);
            }
        }

    }

    public void remove(Set<MinecraftKey> set) {
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            MinecraftKey minecraftkey = (MinecraftKey) iterator.next();
            AdvancementNode advancementnode = (AdvancementNode) this.nodes.get(minecraftkey);

            if (advancementnode == null) {
                AdvancementTree.LOGGER.warn("Told to remove advancement {} but I don't know what that is", minecraftkey);
            } else {
                this.remove(advancementnode);
            }
        }

    }

    public void addAll(Collection<AdvancementHolder> collection) {
        List<AdvancementHolder> list = new ArrayList(collection);

        while (!list.isEmpty()) {
            if (!list.removeIf(this::tryInsert)) {
                AdvancementTree.LOGGER.error("Couldn't load advancements: {}", list);
                break;
            }
        }

        // AdvancementTree.LOGGER.info("Loaded {} advancements", this.nodes.size()); // CraftBukkit - moved to AdvancementDataWorld#reload
    }

    private boolean tryInsert(AdvancementHolder advancementholder) {
        Optional<MinecraftKey> optional = advancementholder.value().parent();
        Map map = this.nodes;

        Objects.requireNonNull(this.nodes);
        AdvancementNode advancementnode = (AdvancementNode) optional.map(map::get).orElse((Object) null);

        if (advancementnode == null && optional.isPresent()) {
            return false;
        } else {
            AdvancementNode advancementnode1 = new AdvancementNode(advancementholder, advancementnode);

            if (advancementnode != null) {
                advancementnode.addChild(advancementnode1);
            }

            this.nodes.put(advancementholder.id(), advancementnode1);
            if (advancementnode == null) {
                this.roots.add(advancementnode1);
                if (this.listener != null) {
                    this.listener.onAddAdvancementRoot(advancementnode1);
                }
            } else {
                this.tasks.add(advancementnode1);
                if (this.listener != null) {
                    this.listener.onAddAdvancementTask(advancementnode1);
                }
            }

            return true;
        }
    }

    public void clear() {
        this.nodes.clear();
        this.roots.clear();
        this.tasks.clear();
        if (this.listener != null) {
            this.listener.onAdvancementsCleared();
        }

    }

    public Iterable<AdvancementNode> roots() {
        return this.roots;
    }

    public Collection<AdvancementNode> nodes() {
        return this.nodes.values();
    }

    @Nullable
    public AdvancementNode get(MinecraftKey minecraftkey) {
        return (AdvancementNode) this.nodes.get(minecraftkey);
    }

    @Nullable
    public AdvancementNode get(AdvancementHolder advancementholder) {
        return (AdvancementNode) this.nodes.get(advancementholder.id());
    }

    public void setListener(@Nullable AdvancementTree.a advancementtree_a) {
        this.listener = advancementtree_a;
        if (advancementtree_a != null) {
            Iterator iterator = this.roots.iterator();

            AdvancementNode advancementnode;

            while (iterator.hasNext()) {
                advancementnode = (AdvancementNode) iterator.next();
                advancementtree_a.onAddAdvancementRoot(advancementnode);
            }

            iterator = this.tasks.iterator();

            while (iterator.hasNext()) {
                advancementnode = (AdvancementNode) iterator.next();
                advancementtree_a.onAddAdvancementTask(advancementnode);
            }
        }

    }

    public interface a {

        void onAddAdvancementRoot(AdvancementNode advancementnode);

        void onRemoveAdvancementRoot(AdvancementNode advancementnode);

        void onAddAdvancementTask(AdvancementNode advancementnode);

        void onRemoveAdvancementTask(AdvancementNode advancementnode);

        void onAdvancementsCleared();
    }
}
