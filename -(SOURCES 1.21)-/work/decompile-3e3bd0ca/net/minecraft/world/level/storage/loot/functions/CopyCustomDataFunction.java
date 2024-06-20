package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.INamable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import org.apache.commons.lang3.mutable.MutableObject;

public class CopyCustomDataFunction extends LootItemFunctionConditional {

    public static final MapCodec<CopyCustomDataFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(NbtProviders.CODEC.fieldOf("source").forGetter((copycustomdatafunction) -> {
            return copycustomdatafunction.source;
        }), CopyCustomDataFunction.b.CODEC.listOf().fieldOf("ops").forGetter((copycustomdatafunction) -> {
            return copycustomdatafunction.operations;
        }))).apply(instance, CopyCustomDataFunction::new);
    });
    private final NbtProvider source;
    private final List<CopyCustomDataFunction.b> operations;

    CopyCustomDataFunction(List<LootItemCondition> list, NbtProvider nbtprovider, List<CopyCustomDataFunction.b> list1) {
        super(list);
        this.source = nbtprovider;
        this.operations = List.copyOf(list1);
    }

    @Override
    public LootItemFunctionType<CopyCustomDataFunction> getType() {
        return LootItemFunctions.COPY_CUSTOM_DATA;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.source.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        NBTBase nbtbase = this.source.get(loottableinfo);

        if (nbtbase == null) {
            return itemstack;
        } else {
            MutableObject<NBTTagCompound> mutableobject = new MutableObject();
            Supplier<NBTBase> supplier = () -> {
                if (mutableobject.getValue() == null) {
                    mutableobject.setValue(((CustomData) itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag());
                }

                return (NBTBase) mutableobject.getValue();
            };

            this.operations.forEach((copycustomdatafunction_b) -> {
                copycustomdatafunction_b.apply(supplier, nbtbase);
            });
            NBTTagCompound nbttagcompound = (NBTTagCompound) mutableobject.getValue();

            if (nbttagcompound != null) {
                CustomData.set(DataComponents.CUSTOM_DATA, itemstack, nbttagcompound);
            }

            return itemstack;
        }
    }

    /** @deprecated */
    @Deprecated
    public static CopyCustomDataFunction.a copyData(NbtProvider nbtprovider) {
        return new CopyCustomDataFunction.a(nbtprovider);
    }

    public static CopyCustomDataFunction.a copyData(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return new CopyCustomDataFunction.a(ContextNbtProvider.forContextEntity(loottableinfo_entitytarget));
    }

    public static class a extends LootItemFunctionConditional.a<CopyCustomDataFunction.a> {

        private final NbtProvider source;
        private final List<CopyCustomDataFunction.b> ops = Lists.newArrayList();

        a(NbtProvider nbtprovider) {
            this.source = nbtprovider;
        }

        public CopyCustomDataFunction.a copy(String s, String s1, CopyCustomDataFunction.c copycustomdatafunction_c) {
            try {
                this.ops.add(new CopyCustomDataFunction.b(ArgumentNBTKey.g.of(s), ArgumentNBTKey.g.of(s1), copycustomdatafunction_c));
                return this;
            } catch (CommandSyntaxException commandsyntaxexception) {
                throw new IllegalArgumentException(commandsyntaxexception);
            }
        }

        public CopyCustomDataFunction.a copy(String s, String s1) {
            return this.copy(s, s1, CopyCustomDataFunction.c.REPLACE);
        }

        @Override
        protected CopyCustomDataFunction.a getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyCustomDataFunction(this.getConditions(), this.source, this.ops);
        }
    }

    private static record b(ArgumentNBTKey.g sourcePath, ArgumentNBTKey.g targetPath, CopyCustomDataFunction.c op) {

        public static final Codec<CopyCustomDataFunction.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ArgumentNBTKey.g.CODEC.fieldOf("source").forGetter(CopyCustomDataFunction.b::sourcePath), ArgumentNBTKey.g.CODEC.fieldOf("target").forGetter(CopyCustomDataFunction.b::targetPath), CopyCustomDataFunction.c.CODEC.fieldOf("op").forGetter(CopyCustomDataFunction.b::op)).apply(instance, CopyCustomDataFunction.b::new);
        });

        public void apply(Supplier<NBTBase> supplier, NBTBase nbtbase) {
            try {
                List<NBTBase> list = this.sourcePath.get(nbtbase);

                if (!list.isEmpty()) {
                    this.op.merge((NBTBase) supplier.get(), this.targetPath, list);
                }
            } catch (CommandSyntaxException commandsyntaxexception) {
                ;
            }

        }
    }

    public static enum c implements INamable {

        REPLACE("replace") {
            @Override
            public void merge(NBTBase nbtbase, ArgumentNBTKey.g argumentnbtkey_g, List<NBTBase> list) throws CommandSyntaxException {
                argumentnbtkey_g.set(nbtbase, (NBTBase) Iterables.getLast(list));
            }
        },
        APPEND("append") {
            @Override
            public void merge(NBTBase nbtbase, ArgumentNBTKey.g argumentnbtkey_g, List<NBTBase> list) throws CommandSyntaxException {
                List<NBTBase> list1 = argumentnbtkey_g.getOrCreate(nbtbase, NBTTagList::new);

                list1.forEach((nbtbase1) -> {
                    if (nbtbase1 instanceof NBTTagList) {
                        list.forEach((nbtbase2) -> {
                            ((NBTTagList) nbtbase1).add(nbtbase2.copy());
                        });
                    }

                });
            }
        },
        MERGE("merge") {
            @Override
            public void merge(NBTBase nbtbase, ArgumentNBTKey.g argumentnbtkey_g, List<NBTBase> list) throws CommandSyntaxException {
                List<NBTBase> list1 = argumentnbtkey_g.getOrCreate(nbtbase, NBTTagCompound::new);

                list1.forEach((nbtbase1) -> {
                    if (nbtbase1 instanceof NBTTagCompound) {
                        list.forEach((nbtbase2) -> {
                            if (nbtbase2 instanceof NBTTagCompound) {
                                ((NBTTagCompound) nbtbase1).merge((NBTTagCompound) nbtbase2);
                            }

                        });
                    }

                });
            }
        };

        public static final Codec<CopyCustomDataFunction.c> CODEC = INamable.fromEnum(CopyCustomDataFunction.c::values);
        private final String name;

        public abstract void merge(NBTBase nbtbase, ArgumentNBTKey.g argumentnbtkey_g, List<NBTBase> list) throws CommandSyntaxException;

        c(final String s) {
            this.name = s;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
