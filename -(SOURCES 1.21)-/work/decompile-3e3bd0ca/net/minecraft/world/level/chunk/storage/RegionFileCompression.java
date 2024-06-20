package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.minecraft.util.FastBufferedInputStream;
import org.slf4j.Logger;

public class RegionFileCompression {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Int2ObjectMap<RegionFileCompression> VERSIONS = new Int2ObjectOpenHashMap();
    private static final Object2ObjectMap<String, RegionFileCompression> VERSIONS_BY_NAME = new Object2ObjectOpenHashMap();
    public static final RegionFileCompression VERSION_GZIP = register(new RegionFileCompression(1, (String) null, (inputstream) -> {
        return new FastBufferedInputStream(new GZIPInputStream(inputstream));
    }, (outputstream) -> {
        return new BufferedOutputStream(new GZIPOutputStream(outputstream));
    }));
    public static final RegionFileCompression VERSION_DEFLATE = register(new RegionFileCompression(2, "deflate", (inputstream) -> {
        return new FastBufferedInputStream(new InflaterInputStream(inputstream));
    }, (outputstream) -> {
        return new BufferedOutputStream(new DeflaterOutputStream(outputstream));
    }));
    public static final RegionFileCompression VERSION_NONE = register(new RegionFileCompression(3, "none", FastBufferedInputStream::new, BufferedOutputStream::new));
    public static final RegionFileCompression VERSION_LZ4 = register(new RegionFileCompression(4, "lz4", (inputstream) -> {
        return new FastBufferedInputStream(new LZ4BlockInputStream(inputstream));
    }, (outputstream) -> {
        return new BufferedOutputStream(new LZ4BlockOutputStream(outputstream));
    }));
    public static final RegionFileCompression VERSION_CUSTOM = register(new RegionFileCompression(127, (String) null, (inputstream) -> {
        throw new UnsupportedOperationException();
    }, (outputstream) -> {
        throw new UnsupportedOperationException();
    }));
    public static final RegionFileCompression DEFAULT = RegionFileCompression.VERSION_DEFLATE;
    private static volatile RegionFileCompression selected = RegionFileCompression.DEFAULT;
    private final int id;
    @Nullable
    private final String optionName;
    private final RegionFileCompression.a<InputStream> inputWrapper;
    private final RegionFileCompression.a<OutputStream> outputWrapper;

    private RegionFileCompression(int i, @Nullable String s, RegionFileCompression.a<InputStream> regionfilecompression_a, RegionFileCompression.a<OutputStream> regionfilecompression_a1) {
        this.id = i;
        this.optionName = s;
        this.inputWrapper = regionfilecompression_a;
        this.outputWrapper = regionfilecompression_a1;
    }

    private static RegionFileCompression register(RegionFileCompression regionfilecompression) {
        RegionFileCompression.VERSIONS.put(regionfilecompression.id, regionfilecompression);
        if (regionfilecompression.optionName != null) {
            RegionFileCompression.VERSIONS_BY_NAME.put(regionfilecompression.optionName, regionfilecompression);
        }

        return regionfilecompression;
    }

    @Nullable
    public static RegionFileCompression fromId(int i) {
        return (RegionFileCompression) RegionFileCompression.VERSIONS.get(i);
    }

    public static void configure(String s) {
        RegionFileCompression regionfilecompression = (RegionFileCompression) RegionFileCompression.VERSIONS_BY_NAME.get(s);

        if (regionfilecompression != null) {
            RegionFileCompression.selected = regionfilecompression;
        } else {
            RegionFileCompression.LOGGER.error("Invalid `region-file-compression` value `{}` in server.properties. Please use one of: {}", s, String.join(", ", RegionFileCompression.VERSIONS_BY_NAME.keySet()));
        }

    }

    public static RegionFileCompression getSelected() {
        return RegionFileCompression.selected;
    }

    public static boolean isValidVersion(int i) {
        return RegionFileCompression.VERSIONS.containsKey(i);
    }

    public int getId() {
        return this.id;
    }

    public OutputStream wrap(OutputStream outputstream) throws IOException {
        return (OutputStream) this.outputWrapper.wrap(outputstream);
    }

    public InputStream wrap(InputStream inputstream) throws IOException {
        return (InputStream) this.inputWrapper.wrap(inputstream);
    }

    @FunctionalInterface
    private interface a<O> {

        O wrap(O o0) throws IOException;
    }
}
