package net.minecraft.commands.functions;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.resources.MinecraftKey;

class FunctionBuilder<T extends ExecutionCommandSource<T>> {

    @Nullable
    private List<UnboundEntryAction<T>> plainEntries = new ArrayList();
    @Nullable
    private List<MacroFunction.a<T>> macroEntries;
    private final List<String> macroArguments = new ArrayList();

    FunctionBuilder() {}

    public void addCommand(UnboundEntryAction<T> unboundentryaction) {
        if (this.macroEntries != null) {
            this.macroEntries.add(new MacroFunction.c<>(unboundentryaction));
        } else {
            this.plainEntries.add(unboundentryaction);
        }

    }

    private int getArgumentIndex(String s) {
        int i = this.macroArguments.indexOf(s);

        if (i == -1) {
            i = this.macroArguments.size();
            this.macroArguments.add(s);
        }

        return i;
    }

    private IntList convertToIndices(List<String> list) {
        IntArrayList intarraylist = new IntArrayList(list.size());
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            intarraylist.add(this.getArgumentIndex(s));
        }

        return intarraylist;
    }

    public void addMacro(String s, int i, T t0) {
        StringTemplate stringtemplate = StringTemplate.fromString(s, i);

        if (this.plainEntries != null) {
            this.macroEntries = new ArrayList(this.plainEntries.size() + 1);
            Iterator iterator = this.plainEntries.iterator();

            while (iterator.hasNext()) {
                UnboundEntryAction<T> unboundentryaction = (UnboundEntryAction) iterator.next();

                this.macroEntries.add(new MacroFunction.c<>(unboundentryaction));
            }

            this.plainEntries = null;
        }

        this.macroEntries.add(new MacroFunction.b<>(stringtemplate, this.convertToIndices(stringtemplate.variables()), t0));
    }

    public CommandFunction<T> build(MinecraftKey minecraftkey) {
        return (CommandFunction) (this.macroEntries != null ? new MacroFunction<>(minecraftkey, this.macroEntries, this.macroArguments) : new PlainTextFunction<>(minecraftkey, this.plainEntries));
    }
}
