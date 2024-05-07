package net.minecraft.advancements.critereon;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.Products.P2;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.IChatBaseComponent;

public interface CriterionConditionValue<T extends Number> {

    SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.range.empty"));
    SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.range.swapped"));

    Optional<T> min();

    Optional<T> max();

    default boolean isAny() {
        return this.min().isEmpty() && this.max().isEmpty();
    }

    default Optional<T> unwrapPoint() {
        Optional<T> optional = this.min();
        Optional<T> optional1 = this.max();

        return optional.equals(optional1) ? optional : Optional.empty();
    }

    static <T extends Number, R extends CriterionConditionValue<T>> Codec<R> createCodec(Codec<T> codec, CriterionConditionValue.a<T, R> criterionconditionvalue_a) {
        Codec<R> codec1 = RecordCodecBuilder.create((instance) -> {
            P2 p2 = instance.group(codec.optionalFieldOf("min").forGetter(CriterionConditionValue::min), codec.optionalFieldOf("max").forGetter(CriterionConditionValue::max));

            Objects.requireNonNull(criterionconditionvalue_a);
            return p2.apply(instance, criterionconditionvalue_a::create);
        });

        return Codec.either(codec1, codec).xmap((either) -> {
            return (CriterionConditionValue) either.map((criterionconditionvalue) -> {
                return criterionconditionvalue;
            }, (number) -> {
                return criterionconditionvalue_a.create(Optional.of(number), Optional.of(number));
            });
        }, (criterionconditionvalue) -> {
            Optional<T> optional = criterionconditionvalue.unwrapPoint();

            return optional.isPresent() ? Either.right((Number) optional.get()) : Either.left(criterionconditionvalue);
        });
    }

    static <T extends Number, R extends CriterionConditionValue<T>> R fromReader(StringReader stringreader, CriterionConditionValue.b<T, R> criterionconditionvalue_b, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier, Function<T, T> function1) throws CommandSyntaxException {
        if (!stringreader.canRead()) {
            throw CriterionConditionValue.ERROR_EMPTY.createWithContext(stringreader);
        } else {
            int i = stringreader.getCursor();

            try {
                Optional<T> optional = readNumber(stringreader, function, supplier).map(function1);
                Optional optional1;

                if (stringreader.canRead(2) && stringreader.peek() == '.' && stringreader.peek(1) == '.') {
                    stringreader.skip();
                    stringreader.skip();
                    optional1 = readNumber(stringreader, function, supplier).map(function1);
                    if (optional.isEmpty() && optional1.isEmpty()) {
                        throw CriterionConditionValue.ERROR_EMPTY.createWithContext(stringreader);
                    }
                } else {
                    optional1 = optional;
                }

                if (optional.isEmpty() && optional1.isEmpty()) {
                    throw CriterionConditionValue.ERROR_EMPTY.createWithContext(stringreader);
                } else {
                    return criterionconditionvalue_b.create(stringreader, optional, optional1);
                }
            } catch (CommandSyntaxException commandsyntaxexception) {
                stringreader.setCursor(i);
                throw new CommandSyntaxException(commandsyntaxexception.getType(), commandsyntaxexception.getRawMessage(), commandsyntaxexception.getInput(), i);
            }
        }
    }

    private static <T extends Number> Optional<T> readNumber(StringReader stringreader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier) throws CommandSyntaxException {
        int i = stringreader.getCursor();

        while (stringreader.canRead() && isAllowedInputChat(stringreader)) {
            stringreader.skip();
        }

        String s = stringreader.getString().substring(i, stringreader.getCursor());

        if (s.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of((Number) function.apply(s));
            } catch (NumberFormatException numberformatexception) {
                throw ((DynamicCommandExceptionType) supplier.get()).createWithContext(stringreader, s);
            }
        }
    }

    private static boolean isAllowedInputChat(StringReader stringreader) {
        char c0 = stringreader.peek();

        return (c0 < '0' || c0 > '9') && c0 != '-' ? (c0 != '.' ? false : !stringreader.canRead(2) || stringreader.peek(1) != '.') : true;
    }

    @FunctionalInterface
    public interface a<T extends Number, R extends CriterionConditionValue<T>> {

        R create(Optional<T> optional, Optional<T> optional1);
    }

    @FunctionalInterface
    public interface b<T extends Number, R extends CriterionConditionValue<T>> {

        R create(StringReader stringreader, Optional<T> optional, Optional<T> optional1) throws CommandSyntaxException;
    }

    public static record DoubleRange(Optional<Double> min, Optional<Double> max, Optional<Double> minSq, Optional<Double> maxSq) implements CriterionConditionValue<Double> {

        public static final CriterionConditionValue.DoubleRange ANY = new CriterionConditionValue.DoubleRange(Optional.empty(), Optional.empty());
        public static final Codec<CriterionConditionValue.DoubleRange> CODEC = CriterionConditionValue.createCodec(Codec.DOUBLE, CriterionConditionValue.DoubleRange::new);

        private DoubleRange(Optional<Double> optional, Optional<Double> optional1) {
            this(optional, optional1, squareOpt(optional), squareOpt(optional1));
        }

        private static CriterionConditionValue.DoubleRange create(StringReader stringreader, Optional<Double> optional, Optional<Double> optional1) throws CommandSyntaxException {
            if (optional.isPresent() && optional1.isPresent() && (Double) optional.get() > (Double) optional1.get()) {
                throw CriterionConditionValue.DoubleRange.ERROR_SWAPPED.createWithContext(stringreader);
            } else {
                return new CriterionConditionValue.DoubleRange(optional, optional1);
            }
        }

        private static Optional<Double> squareOpt(Optional<Double> optional) {
            return optional.map((odouble) -> {
                return odouble * odouble;
            });
        }

        public static CriterionConditionValue.DoubleRange exactly(double d0) {
            return new CriterionConditionValue.DoubleRange(Optional.of(d0), Optional.of(d0));
        }

        public static CriterionConditionValue.DoubleRange between(double d0, double d1) {
            return new CriterionConditionValue.DoubleRange(Optional.of(d0), Optional.of(d1));
        }

        public static CriterionConditionValue.DoubleRange atLeast(double d0) {
            return new CriterionConditionValue.DoubleRange(Optional.of(d0), Optional.empty());
        }

        public static CriterionConditionValue.DoubleRange atMost(double d0) {
            return new CriterionConditionValue.DoubleRange(Optional.empty(), Optional.of(d0));
        }

        public boolean matches(double d0) {
            return this.min.isPresent() && (Double) this.min.get() > d0 ? false : this.max.isEmpty() || (Double) this.max.get() >= d0;
        }

        public boolean matchesSqr(double d0) {
            return this.minSq.isPresent() && (Double) this.minSq.get() > d0 ? false : this.maxSq.isEmpty() || (Double) this.maxSq.get() >= d0;
        }

        public static CriterionConditionValue.DoubleRange fromReader(StringReader stringreader) throws CommandSyntaxException {
            return fromReader(stringreader, (odouble) -> {
                return odouble;
            });
        }

        public static CriterionConditionValue.DoubleRange fromReader(StringReader stringreader, Function<Double, Double> function) throws CommandSyntaxException {
            CriterionConditionValue.b criterionconditionvalue_b = CriterionConditionValue.DoubleRange::create;
            Function function1 = Double::parseDouble;
            BuiltInExceptionProvider builtinexceptionprovider = CommandSyntaxException.BUILT_IN_EXCEPTIONS;

            Objects.requireNonNull(builtinexceptionprovider);
            return (CriterionConditionValue.DoubleRange) CriterionConditionValue.fromReader(stringreader, criterionconditionvalue_b, function1, builtinexceptionprovider::readerInvalidDouble, function);
        }
    }

    public static record IntegerRange(Optional<Integer> min, Optional<Integer> max, Optional<Long> minSq, Optional<Long> maxSq) implements CriterionConditionValue<Integer> {

        public static final CriterionConditionValue.IntegerRange ANY = new CriterionConditionValue.IntegerRange(Optional.empty(), Optional.empty());
        public static final Codec<CriterionConditionValue.IntegerRange> CODEC = CriterionConditionValue.createCodec(Codec.INT, CriterionConditionValue.IntegerRange::new);

        private IntegerRange(Optional<Integer> optional, Optional<Integer> optional1) {
            this(optional, optional1, optional.map((integer) -> {
                return integer.longValue() * integer.longValue();
            }), squareOpt(optional1));
        }

        private static CriterionConditionValue.IntegerRange create(StringReader stringreader, Optional<Integer> optional, Optional<Integer> optional1) throws CommandSyntaxException {
            if (optional.isPresent() && optional1.isPresent() && (Integer) optional.get() > (Integer) optional1.get()) {
                throw CriterionConditionValue.IntegerRange.ERROR_SWAPPED.createWithContext(stringreader);
            } else {
                return new CriterionConditionValue.IntegerRange(optional, optional1);
            }
        }

        private static Optional<Long> squareOpt(Optional<Integer> optional) {
            return optional.map((integer) -> {
                return integer.longValue() * integer.longValue();
            });
        }

        public static CriterionConditionValue.IntegerRange exactly(int i) {
            return new CriterionConditionValue.IntegerRange(Optional.of(i), Optional.of(i));
        }

        public static CriterionConditionValue.IntegerRange between(int i, int j) {
            return new CriterionConditionValue.IntegerRange(Optional.of(i), Optional.of(j));
        }

        public static CriterionConditionValue.IntegerRange atLeast(int i) {
            return new CriterionConditionValue.IntegerRange(Optional.of(i), Optional.empty());
        }

        public static CriterionConditionValue.IntegerRange atMost(int i) {
            return new CriterionConditionValue.IntegerRange(Optional.empty(), Optional.of(i));
        }

        public boolean matches(int i) {
            return this.min.isPresent() && (Integer) this.min.get() > i ? false : this.max.isEmpty() || (Integer) this.max.get() >= i;
        }

        public boolean matchesSqr(long i) {
            return this.minSq.isPresent() && (Long) this.minSq.get() > i ? false : this.maxSq.isEmpty() || (Long) this.maxSq.get() >= i;
        }

        public static CriterionConditionValue.IntegerRange fromReader(StringReader stringreader) throws CommandSyntaxException {
            return fromReader(stringreader, (integer) -> {
                return integer;
            });
        }

        public static CriterionConditionValue.IntegerRange fromReader(StringReader stringreader, Function<Integer, Integer> function) throws CommandSyntaxException {
            CriterionConditionValue.b criterionconditionvalue_b = CriterionConditionValue.IntegerRange::create;
            Function function1 = Integer::parseInt;
            BuiltInExceptionProvider builtinexceptionprovider = CommandSyntaxException.BUILT_IN_EXCEPTIONS;

            Objects.requireNonNull(builtinexceptionprovider);
            return (CriterionConditionValue.IntegerRange) CriterionConditionValue.fromReader(stringreader, criterionconditionvalue_b, function1, builtinexceptionprovider::readerInvalidInt, function);
        }
    }
}
