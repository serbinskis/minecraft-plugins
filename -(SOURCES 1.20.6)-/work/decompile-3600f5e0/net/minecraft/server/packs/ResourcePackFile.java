package net.minecraft.server.packs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.packs.repository.ResourcePackLoader;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class ResourcePackFile extends ResourcePackAbstract {

    static final Logger LOGGER = LogUtils.getLogger();
    private final ResourcePackFile.b zipFileAccess;
    private final String prefix;

    ResourcePackFile(PackLocationInfo packlocationinfo, ResourcePackFile.b resourcepackfile_b, String s) {
        super(packlocationinfo);
        this.zipFileAccess = resourcepackfile_b;
        this.prefix = s;
    }

    private static String getPathFromLocation(EnumResourcePackType enumresourcepacktype, MinecraftKey minecraftkey) {
        return String.format(Locale.ROOT, "%s/%s/%s", enumresourcepacktype.getDirectory(), minecraftkey.getNamespace(), minecraftkey.getPath());
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... astring) {
        return this.getResource(String.join("/", astring));
    }

    @Override
    public IoSupplier<InputStream> getResource(EnumResourcePackType enumresourcepacktype, MinecraftKey minecraftkey) {
        return this.getResource(getPathFromLocation(enumresourcepacktype, minecraftkey));
    }

    private String addPrefix(String s) {
        return this.prefix.isEmpty() ? s : this.prefix + "/" + s;
    }

    @Nullable
    private IoSupplier<InputStream> getResource(String s) {
        ZipFile zipfile = this.zipFileAccess.getOrCreateZipFile();

        if (zipfile == null) {
            return null;
        } else {
            ZipEntry zipentry = zipfile.getEntry(this.addPrefix(s));

            return zipentry == null ? null : IoSupplier.create(zipfile, zipentry);
        }
    }

    @Override
    public Set<String> getNamespaces(EnumResourcePackType enumresourcepacktype) {
        ZipFile zipfile = this.zipFileAccess.getOrCreateZipFile();

        if (zipfile == null) {
            return Set.of();
        } else {
            Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
            Set<String> set = Sets.newHashSet();
            String s = this.addPrefix(enumresourcepacktype.getDirectory() + "/");

            while (enumeration.hasMoreElements()) {
                ZipEntry zipentry = (ZipEntry) enumeration.nextElement();
                String s1 = zipentry.getName();
                String s2 = extractNamespace(s, s1);

                if (!s2.isEmpty()) {
                    if (MinecraftKey.isValidNamespace(s2)) {
                        set.add(s2);
                    } else {
                        ResourcePackFile.LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", s2, this.zipFileAccess.file);
                    }
                }
            }

            return set;
        }
    }

    @VisibleForTesting
    public static String extractNamespace(String s, String s1) {
        if (!s1.startsWith(s)) {
            return "";
        } else {
            int i = s.length();
            int j = s1.indexOf(47, i);

            return j == -1 ? s1.substring(i) : s1.substring(i, j);
        }
    }

    @Override
    public void close() {
        this.zipFileAccess.close();
    }

    @Override
    public void listResources(EnumResourcePackType enumresourcepacktype, String s, String s1, IResourcePack.a iresourcepack_a) {
        ZipFile zipfile = this.zipFileAccess.getOrCreateZipFile();

        if (zipfile != null) {
            Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
            String s2 = enumresourcepacktype.getDirectory();
            String s3 = this.addPrefix(s2 + "/" + s + "/");
            String s4 = s3 + s1 + "/";

            while (enumeration.hasMoreElements()) {
                ZipEntry zipentry = (ZipEntry) enumeration.nextElement();

                if (!zipentry.isDirectory()) {
                    String s5 = zipentry.getName();

                    if (s5.startsWith(s4)) {
                        String s6 = s5.substring(s3.length());
                        MinecraftKey minecraftkey = MinecraftKey.tryBuild(s, s6);

                        if (minecraftkey != null) {
                            iresourcepack_a.accept(minecraftkey, IoSupplier.create(zipfile, zipentry));
                        } else {
                            ResourcePackFile.LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", s, s6);
                        }
                    }
                }
            }

        }
    }

    private static class b implements AutoCloseable {

        final File file;
        @Nullable
        private ZipFile zipFile;
        private boolean failedToLoad;

        b(File file) {
            this.file = file;
        }

        @Nullable
        ZipFile getOrCreateZipFile() {
            if (this.failedToLoad) {
                return null;
            } else {
                if (this.zipFile == null) {
                    try {
                        this.zipFile = new ZipFile(this.file);
                    } catch (IOException ioexception) {
                        ResourcePackFile.LOGGER.error("Failed to open pack {}", this.file, ioexception);
                        this.failedToLoad = true;
                        return null;
                    }
                }

                return this.zipFile;
            }
        }

        public void close() {
            if (this.zipFile != null) {
                IOUtils.closeQuietly(this.zipFile);
                this.zipFile = null;
            }

        }

        protected void finalize() throws Throwable {
            this.close();
            super.finalize();
        }
    }

    public static class a implements ResourcePackLoader.c {

        private final File content;

        public a(Path path) {
            this(path.toFile());
        }

        public a(File file) {
            this.content = file;
        }

        @Override
        public IResourcePack openPrimary(PackLocationInfo packlocationinfo) {
            ResourcePackFile.b resourcepackfile_b = new ResourcePackFile.b(this.content);

            return new ResourcePackFile(packlocationinfo, resourcepackfile_b, "");
        }

        @Override
        public IResourcePack openFull(PackLocationInfo packlocationinfo, ResourcePackLoader.a resourcepackloader_a) {
            ResourcePackFile.b resourcepackfile_b = new ResourcePackFile.b(this.content);
            ResourcePackFile resourcepackfile = new ResourcePackFile(packlocationinfo, resourcepackfile_b, "");
            List<String> list = resourcepackloader_a.overlays();

            if (list.isEmpty()) {
                return resourcepackfile;
            } else {
                List<IResourcePack> list1 = new ArrayList(list.size());
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    String s = (String) iterator.next();

                    list1.add(new ResourcePackFile(packlocationinfo, resourcepackfile_b, s));
                }

                return new CompositePackResources(resourcepackfile, list1);
            }
        }
    }
}
