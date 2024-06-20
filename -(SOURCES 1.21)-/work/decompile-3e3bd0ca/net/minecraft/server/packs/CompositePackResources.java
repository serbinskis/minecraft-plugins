package net.minecraft.server.packs;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.packs.metadata.ResourcePackMetaParser;
import net.minecraft.server.packs.resources.IoSupplier;

public class CompositePackResources implements IResourcePack {

    private final IResourcePack primaryPackResources;
    private final List<IResourcePack> packResourcesStack;

    public CompositePackResources(IResourcePack iresourcepack, List<IResourcePack> list) {
        this.primaryPackResources = iresourcepack;
        List<IResourcePack> list1 = new ArrayList(list.size() + 1);

        list1.addAll(Lists.reverse(list));
        list1.add(iresourcepack);
        this.packResourcesStack = List.copyOf(list1);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... astring) {
        return this.primaryPackResources.getRootResource(astring);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(EnumResourcePackType enumresourcepacktype, MinecraftKey minecraftkey) {
        Iterator iterator = this.packResourcesStack.iterator();

        IoSupplier iosupplier;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            IResourcePack iresourcepack = (IResourcePack) iterator.next();

            iosupplier = iresourcepack.getResource(enumresourcepacktype, minecraftkey);
        } while (iosupplier == null);

        return iosupplier;
    }

    @Override
    public void listResources(EnumResourcePackType enumresourcepacktype, String s, String s1, IResourcePack.a iresourcepack_a) {
        Map<MinecraftKey, IoSupplier<InputStream>> map = new HashMap();
        Iterator iterator = this.packResourcesStack.iterator();

        while (iterator.hasNext()) {
            IResourcePack iresourcepack = (IResourcePack) iterator.next();

            Objects.requireNonNull(map);
            iresourcepack.listResources(enumresourcepacktype, s, s1, map::putIfAbsent);
        }

        map.forEach(iresourcepack_a);
    }

    @Override
    public Set<String> getNamespaces(EnumResourcePackType enumresourcepacktype) {
        Set<String> set = new HashSet();
        Iterator iterator = this.packResourcesStack.iterator();

        while (iterator.hasNext()) {
            IResourcePack iresourcepack = (IResourcePack) iterator.next();

            set.addAll(iresourcepack.getNamespaces(enumresourcepacktype));
        }

        return set;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(ResourcePackMetaParser<T> resourcepackmetaparser) throws IOException {
        return this.primaryPackResources.getMetadataSection(resourcepackmetaparser);
    }

    @Override
    public PackLocationInfo location() {
        return this.primaryPackResources.location();
    }

    @Override
    public void close() {
        this.packResourcesStack.forEach(IResourcePack::close);
    }
}
