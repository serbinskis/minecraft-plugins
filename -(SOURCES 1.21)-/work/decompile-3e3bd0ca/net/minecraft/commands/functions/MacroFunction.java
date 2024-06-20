package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;

public class MacroFunction<T extends ExecutionCommandSource<T>> implements CommandFunction<T> {

    private static final DecimalFormat DECIMAL_FORMAT = (DecimalFormat) SystemUtils.make(new DecimalFormat("#"), (decimalformat) -> {
        decimalformat.setMaximumFractionDigits(15);
        decimalformat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    });
    private static final int MAX_CACHE_ENTRIES = 8;
    private final List<String> parameters;
    private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap(8, 0.25F);
    private final MinecraftKey id;
    private final List<MacroFunction.a<T>> entries;

    public MacroFunction(MinecraftKey minecraftkey, List<MacroFunction.a<T>> list, List<String> list1) {
        this.id = minecraftkey;
        this.entries = list;
        this.parameters = list1;
    }

    @Override
    public MinecraftKey id() {
        return this.id;
    }

    @Override
    public InstantiatedFunction<T> instantiate(@Nullable NBTTagCompound nbttagcompound, CommandDispatcher<T> commanddispatcher) throws FunctionInstantiationException {
        if (nbttagcompound == null) {
            throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.missing_arguments", IChatBaseComponent.translationArg(this.id())));
        } else {
            List<String> list = new ArrayList(this.parameters.size());
            Iterator iterator = this.parameters.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();
                NBTBase nbtbase = nbttagcompound.get(s);

                if (nbtbase == null) {
                    throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.missing_argument", IChatBaseComponent.translationArg(this.id()), s));
                }

                list.add(stringify(nbtbase));
            }

            InstantiatedFunction<T> instantiatedfunction = (InstantiatedFunction) this.cache.getAndMoveToLast(list);

            if (instantiatedfunction != null) {
                return instantiatedfunction;
            } else {
                if (this.cache.size() >= 8) {
                    this.cache.removeFirst();
                }

                InstantiatedFunction<T> instantiatedfunction1 = this.substituteAndParse(this.parameters, list, commanddispatcher);

                this.cache.put(list, instantiatedfunction1);
                return instantiatedfunction1;
            }
        }
    }

    private static String stringify(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagFloat nbttagfloat) {
            return MacroFunction.DECIMAL_FORMAT.format((double) nbttagfloat.getAsFloat());
        } else if (nbtbase instanceof NBTTagDouble nbttagdouble) {
            return MacroFunction.DECIMAL_FORMAT.format(nbttagdouble.getAsDouble());
        } else if (nbtbase instanceof NBTTagByte nbttagbyte) {
            return String.valueOf(nbttagbyte.getAsByte());
        } else if (nbtbase instanceof NBTTagShort nbttagshort) {
            return String.valueOf(nbttagshort.getAsShort());
        } else if (nbtbase instanceof NBTTagLong nbttaglong) {
            return String.valueOf(nbttaglong.getAsLong());
        } else {
            return nbtbase.getAsString();
        }
    }

    private static void lookupValues(List<String> list, IntList intlist, List<String> list1) {
        list1.clear();
        intlist.forEach((i) -> {
            list1.add((String) list.get(i));
        });
    }

    private InstantiatedFunction<T> substituteAndParse(List<String> list, List<String> list1, CommandDispatcher<T> commanddispatcher) throws FunctionInstantiationException {
        List<UnboundEntryAction<T>> list2 = new ArrayList(this.entries.size());
        List<String> list3 = new ArrayList(list1.size());
        Iterator iterator = this.entries.iterator();

        while (iterator.hasNext()) {
            MacroFunction.a<T> macrofunction_a = (MacroFunction.a) iterator.next();

            lookupValues(list1, macrofunction_a.parameters(), list3);
            list2.add(macrofunction_a.instantiate(list3, commanddispatcher, this.id));
        }

        return new PlainTextFunction<>(this.id().withPath((s) -> {
            return s + "/" + list.hashCode();
        }), list2);
    }

    interface a<T> {

        IntList parameters();

        UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commanddispatcher, MinecraftKey minecraftkey) throws FunctionInstantiationException;
    }

    static class b<T extends ExecutionCommandSource<T>> implements MacroFunction.a<T> {

        private final StringTemplate template;
        private final IntList parameters;
        private final T compilationContext;

        public b(StringTemplate stringtemplate, IntList intlist, T t0) {
            this.template = stringtemplate;
            this.parameters = intlist;
            this.compilationContext = t0;
        }

        @Override
        public IntList parameters() {
            return this.parameters;
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commanddispatcher, MinecraftKey minecraftkey) throws FunctionInstantiationException {
            String s = this.template.substitute(list);

            try {
                return CommandFunction.parseCommand(commanddispatcher, this.compilationContext, new StringReader(s));
            } catch (CommandSyntaxException commandsyntaxexception) {
                throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.parse", IChatBaseComponent.translationArg(minecraftkey), s, commandsyntaxexception.getMessage()));
            }
        }
    }

    static class c<T> implements MacroFunction.a<T> {

        private final UnboundEntryAction<T> compiledAction;

        public c(UnboundEntryAction<T> unboundentryaction) {
            this.compiledAction = unboundentryaction;
        }

        @Override
        public IntList parameters() {
            return IntLists.emptyList();
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commanddispatcher, MinecraftKey minecraftkey) {
            return this.compiledAction;
        }
    }
}
