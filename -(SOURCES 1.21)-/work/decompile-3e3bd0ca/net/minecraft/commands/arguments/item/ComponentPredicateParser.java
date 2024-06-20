package net.minecraft.commands.arguments.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.Unit;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.ResourceLocationParseRule;
import net.minecraft.util.parsing.packrat.commands.ResourceLookupRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.TagParseRule;

public class ComponentPredicateParser {

    public ComponentPredicateParser() {}

    public static <T, C, P> Grammar<List<T>> createGrammar(ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
        Atom<List<T>> atom = Atom.of("top");
        Atom<Optional<T>> atom1 = Atom.of("type");
        Atom<Unit> atom2 = Atom.of("any_type");
        Atom<T> atom3 = Atom.of("element_type");
        Atom<T> atom4 = Atom.of("tag_type");
        Atom<List<T>> atom5 = Atom.of("conditions");
        Atom<List<T>> atom6 = Atom.of("alternatives");
        Atom<T> atom7 = Atom.of("term");
        Atom<T> atom8 = Atom.of("negation");
        Atom<T> atom9 = Atom.of("test");
        Atom<C> atom10 = Atom.of("component_type");
        Atom<P> atom11 = Atom.of("predicate_type");
        Atom<MinecraftKey> atom12 = Atom.of("id");
        Atom<NBTBase> atom13 = Atom.of("tag");
        Dictionary<StringReader> dictionary = new Dictionary<>();

        dictionary.put(atom, Term.alternative(Term.sequence(Term.named(atom1), StringReaderTerms.character('['), Term.cut(), Term.optional(Term.named(atom5)), StringReaderTerms.character(']')), Term.named(atom1)), (scope) -> {
            Builder<T> builder = ImmutableList.builder();
            Optional optional = (Optional) scope.getOrThrow(atom1);

            Objects.requireNonNull(builder);
            optional.ifPresent(builder::add);
            List<T> list = (List) scope.get(atom5);

            if (list != null) {
                builder.addAll(list);
            }

            return builder.build();
        });
        dictionary.put(atom1, Term.alternative(Term.named(atom3), Term.sequence(StringReaderTerms.character('#'), Term.cut(), Term.named(atom4)), Term.named(atom2)), (scope) -> {
            return Optional.ofNullable(scope.getAny(atom3, atom4));
        });
        dictionary.put(atom2, StringReaderTerms.character('*'), (scope) -> {
            return Unit.INSTANCE;
        });
        dictionary.put(atom3, new ComponentPredicateParser.c<>(atom12, componentpredicateparser_b));
        dictionary.put(atom4, new ComponentPredicateParser.e<>(atom12, componentpredicateparser_b));
        dictionary.put(atom5, Term.sequence(Term.named(atom6), Term.optional(Term.sequence(StringReaderTerms.character(','), Term.named(atom5)))), (scope) -> {
            T t0 = componentpredicateparser_b.anyOf((List) scope.getOrThrow(atom6));

            return (List) Optional.ofNullable((List) scope.get(atom5)).map((list) -> {
                return SystemUtils.copyAndAdd(t0, list);
            }).orElse(List.of(t0));
        });
        dictionary.put(atom6, Term.sequence(Term.named(atom7), Term.optional(Term.sequence(StringReaderTerms.character('|'), Term.named(atom6)))), (scope) -> {
            T t0 = scope.getOrThrow(atom7);

            return (List) Optional.ofNullable((List) scope.get(atom6)).map((list) -> {
                return SystemUtils.copyAndAdd(t0, list);
            }).orElse(List.of(t0));
        });
        dictionary.put(atom7, Term.alternative(Term.named(atom9), Term.sequence(StringReaderTerms.character('!'), Term.named(atom8))), (scope) -> {
            return scope.getAnyOrThrow(atom9, atom8);
        });
        dictionary.put(atom8, Term.named(atom9), (scope) -> {
            return componentpredicateparser_b.negate(scope.getOrThrow(atom9));
        });
        dictionary.put(atom9, Term.alternative(Term.sequence(Term.named(atom10), StringReaderTerms.character('='), Term.cut(), Term.named(atom13)), Term.sequence(Term.named(atom11), StringReaderTerms.character('~'), Term.cut(), Term.named(atom13)), Term.named(atom10)), (parsestate, scope) -> {
            P p0 = scope.get(atom11);

            try {
                if (p0 != null) {
                    NBTBase nbtbase = (NBTBase) scope.getOrThrow(atom13);

                    return Optional.of(componentpredicateparser_b.createPredicateTest((ImmutableStringReader) parsestate.input(), p0, nbtbase));
                } else {
                    C c0 = scope.getOrThrow(atom10);
                    NBTBase nbtbase1 = (NBTBase) scope.get(atom13);

                    return Optional.of(nbtbase1 != null ? componentpredicateparser_b.createComponentTest((ImmutableStringReader) parsestate.input(), c0, nbtbase1) : componentpredicateparser_b.createComponentTest((ImmutableStringReader) parsestate.input(), c0));
                }
            } catch (CommandSyntaxException commandsyntaxexception) {
                parsestate.errorCollector().store(parsestate.mark(), commandsyntaxexception);
                return Optional.empty();
            }
        });
        dictionary.put(atom10, new ComponentPredicateParser.a<>(atom12, componentpredicateparser_b));
        dictionary.put(atom11, new ComponentPredicateParser.d<>(atom12, componentpredicateparser_b));
        dictionary.put(atom13, TagParseRule.INSTANCE);
        dictionary.put(atom12, ResourceLocationParseRule.INSTANCE);
        return new Grammar<>(dictionary, atom);
    }

    private static class c<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.b<T, C, P>, T> {

        c(Atom<MinecraftKey> atom, ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
            super(atom, componentpredicateparser_b);
        }

        @Override
        protected T validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception {
            return ((ComponentPredicateParser.b) this.context).forElementType(immutablestringreader, minecraftkey);
        }

        @Override
        public Stream<MinecraftKey> possibleResources() {
            return ((ComponentPredicateParser.b) this.context).listElementTypes();
        }
    }

    public interface b<T, C, P> {

        T forElementType(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws CommandSyntaxException;

        Stream<MinecraftKey> listElementTypes();

        T forTagType(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws CommandSyntaxException;

        Stream<MinecraftKey> listTagTypes();

        C lookupComponentType(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws CommandSyntaxException;

        Stream<MinecraftKey> listComponentTypes();

        T createComponentTest(ImmutableStringReader immutablestringreader, C c0, NBTBase nbtbase) throws CommandSyntaxException;

        T createComponentTest(ImmutableStringReader immutablestringreader, C c0);

        P lookupPredicateType(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws CommandSyntaxException;

        Stream<MinecraftKey> listPredicateTypes();

        T createPredicateTest(ImmutableStringReader immutablestringreader, P p0, NBTBase nbtbase) throws CommandSyntaxException;

        T negate(T t0);

        T anyOf(List<T> list);
    }

    private static class e<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.b<T, C, P>, T> {

        e(Atom<MinecraftKey> atom, ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
            super(atom, componentpredicateparser_b);
        }

        @Override
        protected T validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception {
            return ((ComponentPredicateParser.b) this.context).forTagType(immutablestringreader, minecraftkey);
        }

        @Override
        public Stream<MinecraftKey> possibleResources() {
            return ((ComponentPredicateParser.b) this.context).listTagTypes();
        }
    }

    private static class a<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.b<T, C, P>, C> {

        a(Atom<MinecraftKey> atom, ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
            super(atom, componentpredicateparser_b);
        }

        @Override
        protected C validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception {
            return ((ComponentPredicateParser.b) this.context).lookupComponentType(immutablestringreader, minecraftkey);
        }

        @Override
        public Stream<MinecraftKey> possibleResources() {
            return ((ComponentPredicateParser.b) this.context).listComponentTypes();
        }
    }

    private static class d<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.b<T, C, P>, P> {

        d(Atom<MinecraftKey> atom, ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
            super(atom, componentpredicateparser_b);
        }

        @Override
        protected P validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception {
            return ((ComponentPredicateParser.b) this.context).lookupPredicateType(immutablestringreader, minecraftkey);
        }

        @Override
        public Stream<MinecraftKey> possibleResources() {
            return ((ComponentPredicateParser.b) this.context).listPredicateTypes();
        }
    }
}
