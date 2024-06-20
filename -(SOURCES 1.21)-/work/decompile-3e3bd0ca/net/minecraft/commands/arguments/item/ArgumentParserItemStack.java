package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;

public class ArgumentParserItemStack {

    static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.item.id.invalid", object);
    });
    static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("arguments.item.component.unknown", object);
    });
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("arguments.item.component.malformed", object, object1);
    });
    static final SimpleCommandExceptionType ERROR_EXPECTED_COMPONENT = new SimpleCommandExceptionType(IChatBaseComponent.translatable("arguments.item.component.expected"));
    static final DynamicCommandExceptionType ERROR_REPEATED_COMPONENT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("arguments.item.component.repeated", object);
    });
    private static final DynamicCommandExceptionType ERROR_MALFORMED_ITEM = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("arguments.item.malformed", object);
    });
    public static final char SYNTAX_START_COMPONENTS = '[';
    public static final char SYNTAX_END_COMPONENTS = ']';
    public static final char SYNTAX_COMPONENT_SEPARATOR = ',';
    public static final char SYNTAX_COMPONENT_ASSIGNMENT = '=';
    public static final char SYNTAX_REMOVED_COMPONENT = '!';
    static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
    final HolderLookup.b<Item> items;
    final DynamicOps<NBTBase> registryOps;

    public ArgumentParserItemStack(HolderLookup.a holderlookup_a) {
        this.items = holderlookup_a.lookupOrThrow(Registries.ITEM);
        this.registryOps = holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE);
    }

    public ArgumentParserItemStack.a parse(StringReader stringreader) throws CommandSyntaxException {
        final MutableObject<Holder<Item>> mutableobject = new MutableObject();
        final DataComponentPatch.a datacomponentpatch_a = DataComponentPatch.builder();

        this.parse(stringreader, new ArgumentParserItemStack.d(this) {
            @Override
            public void visitItem(Holder<Item> holder) {
                mutableobject.setValue(holder);
            }

            @Override
            public <T> void visitComponent(DataComponentType<T> datacomponenttype, T t0) {
                datacomponentpatch_a.set(datacomponenttype, t0);
            }

            @Override
            public <T> void visitRemovedComponent(DataComponentType<T> datacomponenttype) {
                datacomponentpatch_a.remove(datacomponenttype);
            }
        });
        Holder<Item> holder = (Holder) Objects.requireNonNull((Holder) mutableobject.getValue(), "Parser gave no item");
        DataComponentPatch datacomponentpatch = datacomponentpatch_a.build();

        validateComponents(stringreader, holder, datacomponentpatch);
        return new ArgumentParserItemStack.a(holder, datacomponentpatch);
    }

    private static void validateComponents(StringReader stringreader, Holder<Item> holder, DataComponentPatch datacomponentpatch) throws CommandSyntaxException {
        PatchedDataComponentMap patcheddatacomponentmap = PatchedDataComponentMap.fromPatch(((Item) holder.value()).components(), datacomponentpatch);
        DataResult<Unit> dataresult = ItemStack.validateComponents(patcheddatacomponentmap);

        dataresult.getOrThrow((s) -> {
            return ArgumentParserItemStack.ERROR_MALFORMED_ITEM.createWithContext(stringreader, s);
        });
    }

    public void parse(StringReader stringreader, ArgumentParserItemStack.d argumentparseritemstack_d) throws CommandSyntaxException {
        int i = stringreader.getCursor();

        try {
            (new ArgumentParserItemStack.b(stringreader, argumentparseritemstack_d)).parse();
        } catch (CommandSyntaxException commandsyntaxexception) {
            stringreader.setCursor(i);
            throw commandsyntaxexception;
        }
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsbuilder) {
        StringReader stringreader = new StringReader(suggestionsbuilder.getInput());

        stringreader.setCursor(suggestionsbuilder.getStart());
        ArgumentParserItemStack.c argumentparseritemstack_c = new ArgumentParserItemStack.c();
        ArgumentParserItemStack.b argumentparseritemstack_b = new ArgumentParserItemStack.b(stringreader, argumentparseritemstack_c);

        try {
            argumentparseritemstack_b.parse();
        } catch (CommandSyntaxException commandsyntaxexception) {
            ;
        }

        return argumentparseritemstack_c.resolveSuggestions(suggestionsbuilder, stringreader);
    }

    public interface d {

        default void visitItem(Holder<Item> holder) {}

        default <T> void visitComponent(DataComponentType<T> datacomponenttype, T t0) {}

        default <T> void visitRemovedComponent(DataComponentType<T> datacomponenttype) {}

        default void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> function) {}
    }

    public static record a(Holder<Item> item, DataComponentPatch components) {

    }

    private class b {

        private final StringReader reader;
        private final ArgumentParserItemStack.d visitor;

        b(final StringReader stringreader, final ArgumentParserItemStack.d argumentparseritemstack_d) {
            this.reader = stringreader;
            this.visitor = argumentparseritemstack_d;
        }

        public void parse() throws CommandSyntaxException {
            this.visitor.visitSuggestions(this::suggestItem);
            this.readItem();
            this.visitor.visitSuggestions(this::suggestStartComponents);
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.visitor.visitSuggestions(ArgumentParserItemStack.SUGGEST_NOTHING);
                this.readComponents();
            }

        }

        private void readItem() throws CommandSyntaxException {
            int i = this.reader.getCursor();
            MinecraftKey minecraftkey = MinecraftKey.read(this.reader);

            this.visitor.visitItem((Holder) ArgumentParserItemStack.this.items.get(ResourceKey.create(Registries.ITEM, minecraftkey)).orElseThrow(() -> {
                this.reader.setCursor(i);
                return ArgumentParserItemStack.ERROR_UNKNOWN_ITEM.createWithContext(this.reader, minecraftkey);
            }));
        }

        private void readComponents() throws CommandSyntaxException {
            this.reader.expect('[');
            this.visitor.visitSuggestions(this::suggestComponentAssignmentOrRemoval);
            Set<DataComponentType<?>> set = new ReferenceArraySet();

            while (this.reader.canRead() && this.reader.peek() != ']') {
                this.reader.skipWhitespace();
                DataComponentType datacomponenttype;

                if (this.reader.canRead() && this.reader.peek() == '!') {
                    this.reader.skip();
                    this.visitor.visitSuggestions(this::suggestComponent);
                    datacomponenttype = readComponentType(this.reader);
                    if (!set.add(datacomponenttype)) {
                        throw ArgumentParserItemStack.ERROR_REPEATED_COMPONENT.create(datacomponenttype);
                    }

                    this.visitor.visitRemovedComponent(datacomponenttype);
                    this.visitor.visitSuggestions(ArgumentParserItemStack.SUGGEST_NOTHING);
                    this.reader.skipWhitespace();
                } else {
                    datacomponenttype = readComponentType(this.reader);
                    if (!set.add(datacomponenttype)) {
                        throw ArgumentParserItemStack.ERROR_REPEATED_COMPONENT.create(datacomponenttype);
                    }

                    this.visitor.visitSuggestions(this::suggestAssignment);
                    this.reader.skipWhitespace();
                    this.reader.expect('=');
                    this.visitor.visitSuggestions(ArgumentParserItemStack.SUGGEST_NOTHING);
                    this.reader.skipWhitespace();
                    this.readComponent(datacomponenttype);
                    this.reader.skipWhitespace();
                }

                this.visitor.visitSuggestions(this::suggestNextOrEndComponents);
                if (!this.reader.canRead() || this.reader.peek() != ',') {
                    break;
                }

                this.reader.skip();
                this.reader.skipWhitespace();
                this.visitor.visitSuggestions(this::suggestComponentAssignmentOrRemoval);
                if (!this.reader.canRead()) {
                    throw ArgumentParserItemStack.ERROR_EXPECTED_COMPONENT.createWithContext(this.reader);
                }
            }

            this.reader.expect(']');
            this.visitor.visitSuggestions(ArgumentParserItemStack.SUGGEST_NOTHING);
        }

        public static DataComponentType<?> readComponentType(StringReader stringreader) throws CommandSyntaxException {
            if (!stringreader.canRead()) {
                throw ArgumentParserItemStack.ERROR_EXPECTED_COMPONENT.createWithContext(stringreader);
            } else {
                int i = stringreader.getCursor();
                MinecraftKey minecraftkey = MinecraftKey.read(stringreader);
                DataComponentType<?> datacomponenttype = (DataComponentType) BuiltInRegistries.DATA_COMPONENT_TYPE.get(minecraftkey);

                if (datacomponenttype != null && !datacomponenttype.isTransient()) {
                    return datacomponenttype;
                } else {
                    stringreader.setCursor(i);
                    throw ArgumentParserItemStack.ERROR_UNKNOWN_COMPONENT.createWithContext(stringreader, minecraftkey);
                }
            }
        }

        private <T> void readComponent(DataComponentType<T> datacomponenttype) throws CommandSyntaxException {
            int i = this.reader.getCursor();
            NBTBase nbtbase = (new MojangsonParser(this.reader)).readValue();
            DataResult<T> dataresult = datacomponenttype.codecOrThrow().parse(ArgumentParserItemStack.this.registryOps, nbtbase);

            this.visitor.visitComponent(datacomponenttype, dataresult.getOrThrow((s) -> {
                this.reader.setCursor(i);
                return ArgumentParserItemStack.ERROR_MALFORMED_COMPONENT.createWithContext(this.reader, datacomponenttype.toString(), s);
            }));
        }

        private CompletableFuture<Suggestions> suggestStartComponents(SuggestionsBuilder suggestionsbuilder) {
            if (suggestionsbuilder.getRemaining().isEmpty()) {
                suggestionsbuilder.suggest(String.valueOf('['));
            }

            return suggestionsbuilder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestNextOrEndComponents(SuggestionsBuilder suggestionsbuilder) {
            if (suggestionsbuilder.getRemaining().isEmpty()) {
                suggestionsbuilder.suggest(String.valueOf(','));
                suggestionsbuilder.suggest(String.valueOf(']'));
            }

            return suggestionsbuilder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestAssignment(SuggestionsBuilder suggestionsbuilder) {
            if (suggestionsbuilder.getRemaining().isEmpty()) {
                suggestionsbuilder.suggest(String.valueOf('='));
            }

            return suggestionsbuilder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder suggestionsbuilder) {
            return ICompletionProvider.suggestResource(ArgumentParserItemStack.this.items.listElementIds().map(ResourceKey::location), suggestionsbuilder);
        }

        private CompletableFuture<Suggestions> suggestComponentAssignmentOrRemoval(SuggestionsBuilder suggestionsbuilder) {
            suggestionsbuilder.suggest(String.valueOf('!'));
            return this.suggestComponent(suggestionsbuilder, String.valueOf('='));
        }

        private CompletableFuture<Suggestions> suggestComponent(SuggestionsBuilder suggestionsbuilder) {
            return this.suggestComponent(suggestionsbuilder, "");
        }

        private CompletableFuture<Suggestions> suggestComponent(SuggestionsBuilder suggestionsbuilder, String s) {
            String s1 = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);

            ICompletionProvider.filterResources(BuiltInRegistries.DATA_COMPONENT_TYPE.entrySet(), s1, (entry) -> {
                return ((ResourceKey) entry.getKey()).location();
            }, (entry) -> {
                DataComponentType<?> datacomponenttype = (DataComponentType) entry.getValue();

                if (datacomponenttype.codec() != null) {
                    MinecraftKey minecraftkey = ((ResourceKey) entry.getKey()).location();
                    String s2 = String.valueOf(minecraftkey);

                    suggestionsbuilder.suggest(s2 + s);
                }

            });
            return suggestionsbuilder.buildFuture();
        }
    }

    private static class c implements ArgumentParserItemStack.d {

        private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions;

        c() {
            this.suggestions = ArgumentParserItemStack.SUGGEST_NOTHING;
        }

        @Override
        public void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> function) {
            this.suggestions = function;
        }

        public CompletableFuture<Suggestions> resolveSuggestions(SuggestionsBuilder suggestionsbuilder, StringReader stringreader) {
            return (CompletableFuture) this.suggestions.apply(suggestionsbuilder.createOffset(stringreader.getCursor()));
        }
    }
}
