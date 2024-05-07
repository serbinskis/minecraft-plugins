package net.minecraft.commands.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;

public record StringTemplate(List<String> segments, List<String> variables) {

    public static StringTemplate fromString(String s, int i) {
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

            return new StringTemplate(builder.build(), builder1.build());
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

    public String substitute(List<String> list) {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < this.variables.size(); ++i) {
            stringbuilder.append((String) this.segments.get(i)).append((String) list.get(i));
            CommandFunction.checkCommandLineLength(stringbuilder);
        }

        if (this.segments.size() > this.variables.size()) {
            stringbuilder.append((String) this.segments.get(this.segments.size() - 1));
        }

        CommandFunction.checkCommandLineLength(stringbuilder);
        return stringbuilder.toString();
    }
}
