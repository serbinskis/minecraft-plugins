package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.INamable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;

public class LootItemFunctionCopyNBT extends LootItemFunctionConditional {

    public static final Codec<LootItemFunctionCopyNBT> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(NbtProviders.CODEC.fieldOf("source").forGetter((lootitemfunctioncopynbt) -> {
            return lootitemfunctioncopynbt.source;
        }), LootItemFunctionCopyNBT.b.CODEC.listOf().fieldOf("ops").forGetter((lootitemfunctioncopynbt) -> {
            return lootitemfunctioncopynbt.operations;
        }))).apply(instance, LootItemFunctionCopyNBT::new);
    });
    private final NbtProvider source;
    private final List<LootItemFunctionCopyNBT.b> operations;

    LootItemFunctionCopyNBT(List<LootItemCondition> list, NbtProvider nbtprovider, List<LootItemFunctionCopyNBT.b> list1) {
        super(list);
        this.source = nbtprovider;
        this.operations = List.copyOf(list1);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_NBT;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.source.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        NBTBase nbtbase = this.source.get(loottableinfo);

        if (nbtbase != null) {
            this.operations.forEach((lootitemfunctioncopynbt_b) -> {
                Objects.requireNonNull(itemstack);
                lootitemfunctioncopynbt_b.apply(itemstack::getOrCreateTag, nbtbase);
            });
        }

        return itemstack;
    }

    public static LootItemFunctionCopyNBT.a copyData(NbtProvider nbtprovider) {
        return new LootItemFunctionCopyNBT.a(nbtprovider);
    }

    public static LootItemFunctionCopyNBT.a copyData(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return new LootItemFunctionCopyNBT.a(ContextNbtProvider.forContextEntity(loottableinfo_entitytarget));
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionCopyNBT.a> {

        private final NbtProvider source;
        private final List<LootItemFunctionCopyNBT.b> ops = Lists.newArrayList();

        a(NbtProvider nbtprovider) {
            this.source = nbtprovider;
        }

        public LootItemFunctionCopyNBT.a copy(String s, String s1, LootItemFunctionCopyNBT.Action lootitemfunctioncopynbt_action) {
            try {
                this.ops.add(new LootItemFunctionCopyNBT.b(LootItemFunctionCopyNBT.d.of(s), LootItemFunctionCopyNBT.d.of(s1), lootitemfunctioncopynbt_action));
                return this;
            } catch (CommandSyntaxException commandsyntaxexception) {
                throw new IllegalArgumentException(commandsyntaxexception);
            }
        }

        public LootItemFunctionCopyNBT.a copy(String s, String s1) {
            return this.copy(s, s1, LootItemFunctionCopyNBT.Action.REPLACE);
        }

        @Override
        protected LootItemFunctionCopyNBT.a getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionCopyNBT(this.getConditions(), this.source, this.ops);
        }
    }

    private static record b(LootItemFunctionCopyNBT.d sourcePath, LootItemFunctionCopyNBT.d targetPath, LootItemFunctionCopyNBT.Action op) {

        public static final Codec<LootItemFunctionCopyNBT.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(LootItemFunctionCopyNBT.d.CODEC.fieldOf("source").forGetter(LootItemFunctionCopyNBT.b::sourcePath), LootItemFunctionCopyNBT.d.CODEC.fieldOf("target").forGetter(LootItemFunctionCopyNBT.b::targetPath), LootItemFunctionCopyNBT.Action.CODEC.fieldOf("op").forGetter(LootItemFunctionCopyNBT.b::op)).apply(instance, LootItemFunctionCopyNBT.b::new);
        });

        public void apply(Supplier<NBTBase> supplier, NBTBase nbtbase) {
            try {
                List<NBTBase> list = this.sourcePath.path().get(nbtbase);

                if (!list.isEmpty()) {
                    this.op.merge((NBTBase) supplier.get(), this.targetPath.path(), list);
                }
            } catch (CommandSyntaxException commandsyntaxexception) {
                ;
            }

        }
    }

    public static enum Action implements INamable {

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

        public static final Codec<LootItemFunctionCopyNBT.Action> CODEC = INamable.fromEnum(LootItemFunctionCopyNBT.Action::values);
        private final String name;

        public abstract void merge(NBTBase nbtbase, ArgumentNBTKey.g argumentnbtkey_g, List<NBTBase> list) throws CommandSyntaxException;

        Action(String s) {
            this.name = s;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    private static record d(String string, ArgumentNBTKey.g path) {

        public static final Codec<LootItemFunctionCopyNBT.d> CODEC = Codec.STRING.comapFlatMap((s) -> {
            try {
                return DataResult.success(of(s));
            } catch (CommandSyntaxException commandsyntaxexception) {
                return DataResult.error(() -> {
                    return "Failed to parse path " + s + ": " + commandsyntaxexception.getMessage();
                });
            }
        }, LootItemFunctionCopyNBT.d::string);

        public static LootItemFunctionCopyNBT.d of(String s) throws CommandSyntaxException {
            ArgumentNBTKey.g argumentnbtkey_g = (new ArgumentNBTKey()).parse(new StringReader(s));

            return new LootItemFunctionCopyNBT.d(s, argumentnbtkey_g);
        }
    }
}
