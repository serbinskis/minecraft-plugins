package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface ProblemReporter {

    ProblemReporter forChild(String s);

    void report(String s);

    public static class a implements ProblemReporter {

        private final Multimap<String, String> problems;
        private final Supplier<String> path;
        @Nullable
        private String pathCache;

        public a() {
            this(HashMultimap.create(), () -> {
                return "";
            });
        }

        private a(Multimap<String, String> multimap, Supplier<String> supplier) {
            this.problems = multimap;
            this.path = supplier;
        }

        private String getPath() {
            if (this.pathCache == null) {
                this.pathCache = (String) this.path.get();
            }

            return this.pathCache;
        }

        @Override
        public ProblemReporter forChild(String s) {
            return new ProblemReporter.a(this.problems, () -> {
                String s1 = this.getPath();

                return s1 + s;
            });
        }

        @Override
        public void report(String s) {
            this.problems.put(this.getPath(), s);
        }

        public Multimap<String, String> get() {
            return ImmutableMultimap.copyOf(this.problems);
        }
    }
}
