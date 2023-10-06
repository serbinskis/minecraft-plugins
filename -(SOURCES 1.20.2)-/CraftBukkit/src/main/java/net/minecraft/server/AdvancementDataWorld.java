// mc-dev import
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.LootDeserializationContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceDataJson;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;

public class AdvancementDataWorld extends ResourceDataJson {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = (new GsonBuilder()).create();
    public Map<MinecraftKey, AdvancementHolder> advancements = Map.of();
    private AdvancementTree tree = new AdvancementTree();
    private final LootDataManager lootData;

    public AdvancementDataWorld(LootDataManager lootdatamanager) {
        super(AdvancementDataWorld.GSON, "advancements");
        this.lootData = lootdatamanager;
    }

    protected void apply(Map<MinecraftKey, JsonElement> map, IResourceManager iresourcemanager, GameProfilerFiller gameprofilerfiller) {
        Builder<MinecraftKey, AdvancementHolder> builder = ImmutableMap.builder();

        map.forEach((minecraftkey, jsonelement) -> {
            try {
                JsonObject jsonobject = ChatDeserializer.convertToJsonObject(jsonelement, "advancement");
                Advancement advancement = Advancement.fromJson(jsonobject, new LootDeserializationContext(minecraftkey, this.lootData));

                builder.put(minecraftkey, new AdvancementHolder(minecraftkey, advancement));
            } catch (Exception exception) {
                AdvancementDataWorld.LOGGER.error("Parsing error loading custom advancement {}: {}", minecraftkey, exception.getMessage());
            }

        });
        this.advancements = builder.buildOrThrow();
        AdvancementTree advancementtree = new AdvancementTree();

        advancementtree.addAll(this.advancements.values());
        Iterator iterator = advancementtree.roots().iterator();

        while (iterator.hasNext()) {
            AdvancementNode advancementnode = (AdvancementNode) iterator.next();

            if (advancementnode.holder().value().display().isPresent()) {
                TreeNodePosition.run(advancementnode);
            }
        }

        this.tree = advancementtree;
    }

    @Nullable
    public AdvancementHolder get(MinecraftKey minecraftkey) {
        return (AdvancementHolder) this.advancements.get(minecraftkey);
    }

    public AdvancementTree tree() {
        return this.tree;
    }

    public Collection<AdvancementHolder> getAllAdvancements() {
        return this.advancements.values();
    }
}
