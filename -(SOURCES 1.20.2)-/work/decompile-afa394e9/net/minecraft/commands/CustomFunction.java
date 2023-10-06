package net.minecraft.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.CustomFunctionData;

public class CustomFunction {

    private final CustomFunction.d[] entries;
    final MinecraftKey id;

    public CustomFunction(MinecraftKey minecraftkey, CustomFunction.d[] acustomfunction_d) {
        this.id = minecraftkey;
        this.entries = acustomfunction_d;
    }

    public MinecraftKey getId() {
        return this.id;
    }

    public CustomFunction.d[] getEntries() {
        return this.entries;
    }

    public CustomFunction instantiate(@Nullable NBTTagCompound nbttagcompound, com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> com_mojang_brigadier_commanddispatcher, CommandListenerWrapper commandlistenerwrapper) throws FunctionInstantiationException {
        return this;
    }

    private static boolean shouldConcatenateNextLine(CharSequence charsequence) {
        int i = charsequence.length();

        return i > 0 && charsequence.charAt(i - 1) == '\\';
    }

    public static CustomFunction fromLines(MinecraftKey minecraftkey, com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> com_mojang_brigadier_commanddispatcher, CommandListenerWrapper commandlistenerwrapper, List<String> list) {
        List<CustomFunction.d> list1 = new ArrayList(list.size());
        Set<String> set = new ObjectArraySet();

        for (int i = 0; i < list.size(); ++i) {
            int j = i + 1;
            String s = ((String) list.get(i)).trim();
            String s1;
            String s2;

            if (shouldConcatenateNextLine(s)) {
                StringBuilder stringbuilder = new StringBuilder(s);

                do {
                    ++i;
                    if (i == list.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }

                    stringbuilder.deleteCharAt(stringbuilder.length() - 1);
                    s1 = ((String) list.get(i)).trim();
                    stringbuilder.append(s1);
                } while (shouldConcatenateNextLine(stringbuilder));

                s2 = stringbuilder.toString();
            } else {
                s2 = s;
            }

            StringReader stringreader = new StringReader(s2);

            if (stringreader.canRead() && stringreader.peek() != '#') {
                if (stringreader.peek() == '/') {
                    stringreader.skip();
                    if (stringreader.peek() == '/') {
                        throw new IllegalArgumentException("Unknown or invalid command '" + s2 + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
                    }

                    s1 = stringreader.readUnquotedString();
                    throw new IllegalArgumentException("Unknown or invalid command '" + s2 + "' on line " + j + " (did you mean '" + s1 + "'? Do not use a preceding forwards slash.)");
                }

                if (stringreader.peek() == '$') {
                    CustomFunction.f customfunction_f = decomposeMacro(s2.substring(1), j);

                    list1.add(customfunction_f);
                    set.addAll(customfunction_f.parameters());
                } else {
                    try {
                        ParseResults<CommandListenerWrapper> parseresults = com_mojang_brigadier_commanddispatcher.parse(stringreader, commandlistenerwrapper);

                        if (parseresults.getReader().canRead()) {
                            throw CommandDispatcher.getParseException(parseresults);
                        }

                        list1.add(new CustomFunction.b(parseresults));
                    } catch (CommandSyntaxException commandsyntaxexception) {
                        throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + commandsyntaxexception.getMessage());
                    }
                }
            }
        }

        if (set.isEmpty()) {
            return new CustomFunction(minecraftkey, (CustomFunction.d[]) list1.toArray((k) -> {
                return new CustomFunction.d[k];
            }));
        } else {
            return new CustomFunction.c(minecraftkey, (CustomFunction.d[]) list1.toArray((k) -> {
                return new CustomFunction.d[k];
            }), List.copyOf(set));
        }
    }

    @VisibleForTesting
    public static CustomFunction.f decomposeMacro(String s, int i) {
        Builder<String> builder = ImmutableList.builder();
        Builder<String> builder1 = ImmutableList.builder();
        int j = s.length();
        int k = 0;
        int l = s.indexOf(36);

        while (l != -1) {
            if (l != j - 1 && s.charAt(l + 1) == '(') {
                builder.add(s.substring(k, l));
                int i1 = s.indexOf(41, l + 1);

                if (i1 == -1) {
                    throw new IllegalArgumentException("Unterminated macro variable in macro '" + s + "' on line " + i);
                }

                String s1 = s.substring(l + 2, i1);

                if (!isValidVariableName(s1)) {
                    throw new IllegalArgumentException("Invalid macro variable name '" + s1 + "' on line " + i);
                }

                builder1.add(s1);
                k = i1 + 1;
                l = s.indexOf(36, k);
            } else {
                l = s.indexOf(36, l + 1);
            }
        }

        if (k == 0) {
            throw new IllegalArgumentException("Macro without variables on line " + i);
        } else {
            if (k != j) {
                builder.add(s.substring(k));
            }

            return new CustomFunction.f(builder.build(), builder1.build());
        }
    }

    private static boolean isValidVariableName(String s) {
        for (int i = 0; i < s.length(); ++i) {
            char c0 = s.charAt(i);

            if (!Character.isLetterOrDigit(c0) && c0 != '_') {
                return false;
            }
        }

        return true;
    }

    @FunctionalInterface
    public interface d {

        void execute(CustomFunctionData customfunctiondata, CommandListenerWrapper commandlistenerwrapper, Deque<CustomFunctionData.QueuedCommand> deque, int i, int j, @Nullable CustomFunctionData.TraceCallbacks customfunctiondata_tracecallbacks) throws CommandSyntaxException;
    }

    public static class f implements CustomFunction.d {

        private final List<String> segments;
        private final List<String> parameters;

        public f(List<String> list, List<String> list1) {
            this.segments = list;
            this.parameters = list1;
        }

        public List<String> parameters() {
            return this.parameters;
        }

        public String substitute(List<String> list) {
            StringBuilder stringbuilder = new StringBuilder();

            for (int i = 0; i < this.parameters.size(); ++i) {
                stringbuilder.append((String) this.segments.get(i)).append((String) list.get(i));
            }

            if (this.segments.size() > this.parameters.size()) {
                stringbuilder.append((String) this.segments.get(this.segments.size() - 1));
            }

            return stringbuilder.toString();
        }

        @Override
        public void execute(CustomFunctionData customfunctiondata, CommandListenerWrapper commandlistenerwrapper, Deque<CustomFunctionData.QueuedCommand> deque, int i, int j, @Nullable CustomFunctionData.TraceCallbacks customfunctiondata_tracecallbacks) throws CommandSyntaxException {
            throw new IllegalStateException("Tried to execute an uninstantiated macro");
        }
    }

    public static class b implements CustomFunction.d {

        private final ParseResults<CommandListenerWrapper> parse;

        public b(ParseResults<CommandListenerWrapper> parseresults) {
            this.parse = parseresults;
        }

        @Override
        public void execute(CustomFunctionData customfunctiondata, CommandListenerWrapper commandlistenerwrapper, Deque<CustomFunctionData.QueuedCommand> deque, int i, int j, @Nullable CustomFunctionData.TraceCallbacks customfunctiondata_tracecallbacks) throws CommandSyntaxException {
            if (customfunctiondata_tracecallbacks != null) {
                String s = this.parse.getReader().getString();

                customfunctiondata_tracecallbacks.onCommand(j, s);
                int k = this.execute(customfunctiondata, commandlistenerwrapper);

                customfunctiondata_tracecallbacks.onReturn(j, s, k);
            } else {
                this.execute(customfunctiondata, commandlistenerwrapper);
            }

        }

        private int execute(CustomFunctionData customfunctiondata, CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
            return customfunctiondata.getDispatcher().execute(CommandDispatcher.mapSource(this.parse, (commandlistenerwrapper1) -> {
                return commandlistenerwrapper;
            }));
        }

        public String toString() {
            return this.parse.getReader().getString();
        }
    }

    private static class c extends CustomFunction {

        private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");
        private final List<String> parameters;
        private static final int MAX_CACHE_ENTRIES = 8;
        private final Object2ObjectLinkedOpenHashMap<List<String>, CustomFunction> cache = new Object2ObjectLinkedOpenHashMap(8, 0.25F);

        public c(MinecraftKey minecraftkey, CustomFunction.d[] acustomfunction_d, List<String> list) {
            super(minecraftkey, acustomfunction_d);
            this.parameters = list;
        }

        @Override
        public CustomFunction instantiate(@Nullable NBTTagCompound nbttagcompound, com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> com_mojang_brigadier_commanddispatcher, CommandListenerWrapper commandlistenerwrapper) throws FunctionInstantiationException {
            if (nbttagcompound == null) {
                throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.missing_arguments", this.getId()));
            } else {
                List<String> list = new ArrayList(this.parameters.size());
                Iterator iterator = this.parameters.iterator();

                while (iterator.hasNext()) {
                    String s = (String) iterator.next();

                    if (!nbttagcompound.contains(s)) {
                        throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.missing_argument", this.getId(), s));
                    }

                    list.add(stringify(nbttagcompound.get(s)));
                }

                CustomFunction customfunction = (CustomFunction) this.cache.getAndMoveToLast(list);

                if (customfunction != null) {
                    return customfunction;
                } else {
                    if (this.cache.size() >= 8) {
                        this.cache.removeFirst();
                    }

                    CustomFunction customfunction1 = this.substituteAndParse(list, com_mojang_brigadier_commanddispatcher, commandlistenerwrapper);

                    if (customfunction1 != null) {
                        this.cache.put(list, customfunction1);
                    }

                    return customfunction1;
                }
            }
        }

        private static String stringify(NBTBase nbtbase) {
            if (nbtbase instanceof NBTTagFloat) {
                NBTTagFloat nbttagfloat = (NBTTagFloat) nbtbase;

                return CustomFunction.c.DECIMAL_FORMAT.format((double) nbttagfloat.getAsFloat());
            } else if (nbtbase instanceof NBTTagDouble) {
                NBTTagDouble nbttagdouble = (NBTTagDouble) nbtbase;

                return CustomFunction.c.DECIMAL_FORMAT.format(nbttagdouble.getAsDouble());
            } else if (nbtbase instanceof NBTTagByte) {
                NBTTagByte nbttagbyte = (NBTTagByte) nbtbase;

                return String.valueOf(nbttagbyte.getAsByte());
            } else if (nbtbase instanceof NBTTagShort) {
                NBTTagShort nbttagshort = (NBTTagShort) nbtbase;

                return String.valueOf(nbttagshort.getAsShort());
            } else if (nbtbase instanceof NBTTagLong) {
                NBTTagLong nbttaglong = (NBTTagLong) nbtbase;

                return String.valueOf(nbttaglong.getAsLong());
            } else {
                return nbtbase.getAsString();
            }
        }

        private CustomFunction substituteAndParse(List<String> list, com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> com_mojang_brigadier_commanddispatcher, CommandListenerWrapper commandlistenerwrapper) throws FunctionInstantiationException {
            CustomFunction.d[] acustomfunction_d = this.getEntries();
            CustomFunction.d[] acustomfunction_d1 = new CustomFunction.d[acustomfunction_d.length];

            for (int i = 0; i < acustomfunction_d.length; ++i) {
                CustomFunction.d customfunction_d = acustomfunction_d[i];

                if (customfunction_d instanceof CustomFunction.f) {
                    CustomFunction.f customfunction_f = (CustomFunction.f) customfunction_d;
                    List<String> list1 = customfunction_f.parameters();
                    List<String> list2 = new ArrayList(list1.size());
                    Iterator iterator = list1.iterator();

                    while (iterator.hasNext()) {
                        String s = (String) iterator.next();

                        list2.add((String) list.get(this.parameters.indexOf(s)));
                    }

                    String s1 = customfunction_f.substitute(list2);

                    try {
                        ParseResults<CommandListenerWrapper> parseresults = com_mojang_brigadier_commanddispatcher.parse(s1, commandlistenerwrapper);

                        if (parseresults.getReader().canRead()) {
                            throw CommandDispatcher.getParseException(parseresults);
                        }

                        acustomfunction_d1[i] = new CustomFunction.b(parseresults);
                    } catch (CommandSyntaxException commandsyntaxexception) {
                        throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.parse", this.getId(), s1, commandsyntaxexception.getMessage()));
                    }
                } else {
                    acustomfunction_d1[i] = customfunction_d;
                }
            }

            MinecraftKey minecraftkey = this.getId();
            String s2 = minecraftkey.getNamespace();
            String s3 = minecraftkey.getPath();

            return new CustomFunction(new MinecraftKey(s2, s3 + "/" + list.hashCode()), acustomfunction_d1);
        }

        static {
            CustomFunction.c.DECIMAL_FORMAT.setMaximumFractionDigits(15);
            CustomFunction.c.DECIMAL_FORMAT.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        }
    }

    public static class a {

        public static final CustomFunction.a NONE = new CustomFunction.a((MinecraftKey) null);
        @Nullable
        private final MinecraftKey id;
        private boolean resolved;
        private Optional<CustomFunction> function = Optional.empty();

        public a(@Nullable MinecraftKey minecraftkey) {
            this.id = minecraftkey;
        }

        public a(CustomFunction customfunction) {
            this.resolved = true;
            this.id = null;
            this.function = Optional.of(customfunction);
        }

        public Optional<CustomFunction> get(CustomFunctionData customfunctiondata) {
            if (!this.resolved) {
                if (this.id != null) {
                    this.function = customfunctiondata.get(this.id);
                }

                this.resolved = true;
            }

            return this.function;
        }

        @Nullable
        public MinecraftKey getId() {
            return (MinecraftKey) this.function.map((customfunction) -> {
                return customfunction.id;
            }).orElse(this.id);
        }
    }

    public static class e implements CustomFunction.d {

        private final CustomFunction.a function;

        public e(CustomFunction customfunction) {
            this.function = new CustomFunction.a(customfunction);
        }

        @Override
        public void execute(CustomFunctionData customfunctiondata, CommandListenerWrapper commandlistenerwrapper, Deque<CustomFunctionData.QueuedCommand> deque, int i, int j, @Nullable CustomFunctionData.TraceCallbacks customfunctiondata_tracecallbacks) {
            SystemUtils.ifElse(this.function.get(customfunctiondata), (customfunction) -> {
                CustomFunction.d[] acustomfunction_d = customfunction.getEntries();

                if (customfunctiondata_tracecallbacks != null) {
                    customfunctiondata_tracecallbacks.onCall(j, customfunction.getId(), acustomfunction_d.length);
                }

                int k = i - deque.size();
                int l = Math.min(acustomfunction_d.length, k);

                for (int i1 = l - 1; i1 >= 0; --i1) {
                    deque.addFirst(new CustomFunctionData.QueuedCommand(commandlistenerwrapper, j + 1, acustomfunction_d[i1]));
                }

            }, () -> {
                if (customfunctiondata_tracecallbacks != null) {
                    customfunctiondata_tracecallbacks.onCall(j, this.function.getId(), -1);
                }

            });
        }

        public String toString() {
            return "function " + this.function.getId();
        }
    }
}
