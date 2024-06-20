package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.server.packs.metadata.ResourcePackMetaParser;
import net.minecraft.util.ChatDeserializer;

public interface ResourceMetadata {

    ResourceMetadata EMPTY = new ResourceMetadata() {
        @Override
        public <T> Optional<T> getSection(ResourcePackMetaParser<T> resourcepackmetaparser) {
            return Optional.empty();
        }
    };
    IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> {
        return ResourceMetadata.EMPTY;
    };

    static ResourceMetadata fromJsonStream(InputStream inputstream) throws IOException {
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));

        ResourceMetadata resourcemetadata;

        try {
            final JsonObject jsonobject = ChatDeserializer.parse((Reader) bufferedreader);

            resourcemetadata = new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(ResourcePackMetaParser<T> resourcepackmetaparser) {
                    String s = resourcepackmetaparser.getMetadataSectionName();

                    return jsonobject.has(s) ? Optional.of(resourcepackmetaparser.fromJson(ChatDeserializer.getAsJsonObject(jsonobject, s))) : Optional.empty();
                }
            };
        } catch (Throwable throwable) {
            try {
                bufferedreader.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }

            throw throwable;
        }

        bufferedreader.close();
        return resourcemetadata;
    }

    <T> Optional<T> getSection(ResourcePackMetaParser<T> resourcepackmetaparser);

    default ResourceMetadata copySections(Collection<ResourcePackMetaParser<?>> collection) {
        ResourceMetadata.a resourcemetadata_a = new ResourceMetadata.a();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ResourcePackMetaParser<?> resourcepackmetaparser = (ResourcePackMetaParser) iterator.next();

            this.copySection(resourcemetadata_a, resourcepackmetaparser);
        }

        return resourcemetadata_a.build();
    }

    private <T> void copySection(ResourceMetadata.a resourcemetadata_a, ResourcePackMetaParser<T> resourcepackmetaparser) {
        this.getSection(resourcepackmetaparser).ifPresent((object) -> {
            resourcemetadata_a.put(resourcepackmetaparser, object);
        });
    }

    public static class a {

        private final Builder<ResourcePackMetaParser<?>, Object> map = ImmutableMap.builder();

        public a() {}

        public <T> ResourceMetadata.a put(ResourcePackMetaParser<T> resourcepackmetaparser, T t0) {
            this.map.put(resourcepackmetaparser, t0);
            return this;
        }

        public ResourceMetadata build() {
            final ImmutableMap<ResourcePackMetaParser<?>, Object> immutablemap = this.map.build();

            return immutablemap.isEmpty() ? ResourceMetadata.EMPTY : new ResourceMetadata(this) {
                @Override
                public <T> Optional<T> getSection(ResourcePackMetaParser<T> resourcepackmetaparser) {
                    return Optional.ofNullable(immutablemap.get(resourcepackmetaparser));
                }
            };
        }
    }
}
