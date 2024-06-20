package net.minecraft.data;

import java.nio.file.Path;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public class PackOutput {

    private final Path outputFolder;

    public PackOutput(Path path) {
        this.outputFolder = path;
    }

    public Path getOutputFolder() {
        return this.outputFolder;
    }

    public Path getOutputFolder(PackOutput.b packoutput_b) {
        return this.getOutputFolder().resolve(packoutput_b.directory);
    }

    public PackOutput.a createPathProvider(PackOutput.b packoutput_b, String s) {
        return new PackOutput.a(this, packoutput_b, s);
    }

    public PackOutput.a createRegistryElementsPathProvider(ResourceKey<? extends IRegistry<?>> resourcekey) {
        return this.createPathProvider(PackOutput.b.DATA_PACK, Registries.elementsDirPath(resourcekey));
    }

    public PackOutput.a createRegistryTagsPathProvider(ResourceKey<? extends IRegistry<?>> resourcekey) {
        return this.createPathProvider(PackOutput.b.DATA_PACK, Registries.tagsDirPath(resourcekey));
    }

    public static enum b {

        DATA_PACK("data"), RESOURCE_PACK("assets"), REPORTS("reports");

        final String directory;

        private b(final String s) {
            this.directory = s;
        }
    }

    public static class a {

        private final Path root;
        private final String kind;

        a(PackOutput packoutput, PackOutput.b packoutput_b, String s) {
            this.root = packoutput.getOutputFolder(packoutput_b);
            this.kind = s;
        }

        public Path file(MinecraftKey minecraftkey, String s) {
            Path path = this.root.resolve(minecraftkey.getNamespace()).resolve(this.kind);
            String s1 = minecraftkey.getPath();

            return path.resolve(s1 + "." + s);
        }

        public Path json(MinecraftKey minecraftkey) {
            return this.root.resolve(minecraftkey.getNamespace()).resolve(this.kind).resolve(minecraftkey.getPath() + ".json");
        }
    }
}
