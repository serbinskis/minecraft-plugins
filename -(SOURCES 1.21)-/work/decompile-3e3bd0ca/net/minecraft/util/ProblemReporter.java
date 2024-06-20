package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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

        public Optional<String> getReport() {
            Multimap<String, String> multimap = this.get();

            if (!multimap.isEmpty()) {
                String s = (String) multimap.asMap().entrySet().stream().map((entry) -> {
                    String s1 = (String) entry.getKey();

                    return " at " + s1 + ": " + String.join("; ", (Iterable) entry.getValue());
                }).collect(Collectors.joining("\n"));

                return Optional.of(s);
            } else {
                return Optional.empty();
            }
        }
    }
}
