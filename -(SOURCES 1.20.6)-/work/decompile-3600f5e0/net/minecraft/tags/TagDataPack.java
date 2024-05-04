package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.packs.resources.IResource;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.util.DependencySorter;
import org.slf4j.Logger;

public class TagDataPack<T> {

    private static final Logger LOGGER = LogUtils.getLogger();
    final Function<MinecraftKey, Optional<? extends T>> idToValue;
    private final String directory;

    public TagDataPack(Function<MinecraftKey, Optional<? extends T>> function, String s) {
        this.idToValue = function;
        this.directory = s;
    }

    public Map<MinecraftKey, List<TagDataPack.a>> load(IResourceManager iresourcemanager) {
        Map<MinecraftKey, List<TagDataPack.a>> map = Maps.newHashMap();
        FileToIdConverter filetoidconverter = FileToIdConverter.json(this.directory);
        Iterator iterator = filetoidconverter.listMatchingResourceStacks(iresourcemanager).entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MinecraftKey, List<IResource>> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getKey();
            MinecraftKey minecraftkey1 = filetoidconverter.fileToId(minecraftkey);
            Iterator iterator1 = ((List) entry.getValue()).iterator();

            while (iterator1.hasNext()) {
                IResource iresource = (IResource) iterator1.next();

                try {
                    BufferedReader bufferedreader = iresource.openAsReader();

                    try {
                        JsonElement jsonelement = JsonParser.parseReader(bufferedreader);
                        List<TagDataPack.a> list = (List) map.computeIfAbsent(minecraftkey1, (minecraftkey2) -> {
                            return new ArrayList();
                        });
                        TagFile tagfile = (TagFile) TagFile.CODEC.parse(new Dynamic(JsonOps.INSTANCE, jsonelement)).getOrThrow();

                        if (tagfile.replace()) {
                            list.clear();
                        }

                        String s = iresource.sourcePackId();

                        tagfile.entries().forEach((tagentry) -> {
                            list.add(new TagDataPack.a(tagentry, s));
                        });
                    } catch (Throwable throwable) {
                        if (bufferedreader != null) {
                            try {
                                bufferedreader.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        }

                        throw throwable;
                    }

                    if (bufferedreader != null) {
                        bufferedreader.close();
                    }
                } catch (Exception exception) {
                    TagDataPack.LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{minecraftkey1, minecraftkey, iresource.sourcePackId(), exception});
                }
            }
        }

        return map;
    }

    private Either<Collection<TagDataPack.a>, Collection<T>> build(TagEntry.a<T> tagentry_a, List<TagDataPack.a> list) {
        Builder<T> builder = ImmutableSet.builder();
        List<TagDataPack.a> list1 = new ArrayList();
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            TagDataPack.a tagdatapack_a = (TagDataPack.a) iterator.next();
            TagEntry tagentry = tagdatapack_a.entry();

            Objects.requireNonNull(builder);
            if (!tagentry.build(tagentry_a, builder::add)) {
                list1.add(tagdatapack_a);
            }
        }

        return list1.isEmpty() ? Either.right(builder.build()) : Either.left(list1);
    }

    public Map<MinecraftKey, Collection<T>> build(Map<MinecraftKey, List<TagDataPack.a>> map) {
        final Map<MinecraftKey, Collection<T>> map1 = Maps.newHashMap();
        TagEntry.a<T> tagentry_a = new TagEntry.a<T>() {
            @Nullable
            @Override
            public T element(MinecraftKey minecraftkey) {
                return ((Optional) TagDataPack.this.idToValue.apply(minecraftkey)).orElse((Object) null);
            }

            @Nullable
            @Override
            public Collection<T> tag(MinecraftKey minecraftkey) {
                return (Collection) map1.get(minecraftkey);
            }
        };
        DependencySorter<MinecraftKey, TagDataPack.b> dependencysorter = new DependencySorter<>();

        map.forEach((minecraftkey, list) -> {
            dependencysorter.addEntry(minecraftkey, new TagDataPack.b(list));
        });
        dependencysorter.orderByDependencies((minecraftkey, tagdatapack_b) -> {
            this.build(tagentry_a, tagdatapack_b.entries).ifLeft((collection) -> {
                TagDataPack.LOGGER.error("Couldn't load tag {} as it is missing following references: {}", minecraftkey, collection.stream().map(Objects::toString).collect(Collectors.joining(", ")));
            }).ifRight((collection) -> {
                map1.put(minecraftkey, collection);
            });
        });
        return map1;
    }

    public Map<MinecraftKey, Collection<T>> loadAndBuild(IResourceManager iresourcemanager) {
        return this.build(this.load(iresourcemanager));
    }

    public static record a(TagEntry entry, String source) {

        public String toString() {
            String s = String.valueOf(this.entry);

            return s + " (from " + this.source + ")";
        }
    }

    private static record b(List<TagDataPack.a> entries) implements DependencySorter.a<MinecraftKey> {

        @Override
        public void visitRequiredDependencies(Consumer<MinecraftKey> consumer) {
            this.entries.forEach((tagdatapack_a) -> {
                tagdatapack_a.entry.visitRequiredDependencies(consumer);
            });
        }

        @Override
        public void visitOptionalDependencies(Consumer<MinecraftKey> consumer) {
            this.entries.forEach((tagdatapack_a) -> {
                tagdatapack_a.entry.visitOptionalDependencies(consumer);
            });
        }
    }
}
