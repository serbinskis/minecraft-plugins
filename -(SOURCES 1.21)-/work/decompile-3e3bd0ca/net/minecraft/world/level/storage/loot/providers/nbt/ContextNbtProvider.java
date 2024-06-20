package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.CriterionConditionNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class ContextNbtProvider implements NbtProvider {

    private static final String BLOCK_ENTITY_ID = "block_entity";
    private static final ContextNbtProvider.a BLOCK_ENTITY_PROVIDER = new ContextNbtProvider.a() {
        @Override
        public NBTBase get(LootTableInfo loottableinfo) {
            TileEntity tileentity = (TileEntity) loottableinfo.getParamOrNull(LootContextParameters.BLOCK_ENTITY);

            return tileentity != null ? tileentity.saveWithFullMetadata(tileentity.getLevel().registryAccess()) : null;
        }

        @Override
        public String getId() {
            return "block_entity";
        }

        @Override
        public Set<LootContextParameter<?>> getReferencedContextParams() {
            return ImmutableSet.of(LootContextParameters.BLOCK_ENTITY);
        }
    };
    public static final ContextNbtProvider BLOCK_ENTITY = new ContextNbtProvider(ContextNbtProvider.BLOCK_ENTITY_PROVIDER);
    private static final Codec<ContextNbtProvider.a> GETTER_CODEC = Codec.STRING.xmap((s) -> {
        if (s.equals("block_entity")) {
            return ContextNbtProvider.BLOCK_ENTITY_PROVIDER;
        } else {
            LootTableInfo.EntityTarget loottableinfo_entitytarget = LootTableInfo.EntityTarget.getByName(s);

            return forEntity(loottableinfo_entitytarget);
        }
    }, ContextNbtProvider.a::getId);
    public static final MapCodec<ContextNbtProvider> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ContextNbtProvider.GETTER_CODEC.fieldOf("target").forGetter((contextnbtprovider) -> {
            return contextnbtprovider.getter;
        })).apply(instance, ContextNbtProvider::new);
    });
    public static final Codec<ContextNbtProvider> INLINE_CODEC = ContextNbtProvider.GETTER_CODEC.xmap(ContextNbtProvider::new, (contextnbtprovider) -> {
        return contextnbtprovider.getter;
    });
    private final ContextNbtProvider.a getter;

    private static ContextNbtProvider.a forEntity(final LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return new ContextNbtProvider.a() {
            @Nullable
            @Override
            public NBTBase get(LootTableInfo loottableinfo) {
                Entity entity = (Entity) loottableinfo.getParamOrNull(loottableinfo_entitytarget.getParam());

                return entity != null ? CriterionConditionNBT.getEntityTagToCompare(entity) : null;
            }

            @Override
            public String getId() {
                return loottableinfo_entitytarget.name();
            }

            @Override
            public Set<LootContextParameter<?>> getReferencedContextParams() {
                return ImmutableSet.of(loottableinfo_entitytarget.getParam());
            }
        };
    }

    private ContextNbtProvider(ContextNbtProvider.a contextnbtprovider_a) {
        this.getter = contextnbtprovider_a;
    }

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.CONTEXT;
    }

    @Nullable
    @Override
    public NBTBase get(LootTableInfo loottableinfo) {
        return this.getter.get(loottableinfo);
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.getter.getReferencedContextParams();
    }

    public static NbtProvider forContextEntity(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return new ContextNbtProvider(forEntity(loottableinfo_entitytarget));
    }

    private interface a {

        @Nullable
        NBTBase get(LootTableInfo loottableinfo);

        String getId();

        Set<LootContextParameter<?>> getReferencedContextParams();
    }
}
