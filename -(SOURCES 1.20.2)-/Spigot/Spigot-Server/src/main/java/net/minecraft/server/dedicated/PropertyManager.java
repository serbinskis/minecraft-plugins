package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.IRegistryCustom;
import org.slf4j.Logger;

import joptsimple.OptionSet; // CraftBukkit

public abstract class PropertyManager<T extends PropertyManager<T>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    public final Properties properties;
    // CraftBukkit start
    private OptionSet options = null;

    public PropertyManager(Properties properties, final OptionSet options) {
        this.properties = properties;

        this.options = options;
    }

    private String getOverride(String name, String value) {
        if ((this.options != null) && (this.options.has(name)) && !name.equals( "online-mode")) { // Spigot
            return String.valueOf(this.options.valueOf(name));
        }

        return value;
        // CraftBukkit end
    }

    public static Properties loadFromFile(Path path) {
        try {
            // CraftBukkit start - SPIGOT-7465, MC-264979: Don't load if file doesn't exist
            if (!path.toFile().exists()) {
                return new Properties();
            }
            // CraftBukkit end
            Properties properties;
            Properties properties1;

            try {
                InputStream inputstream = Files.newInputStream(path);

                try {
                    CharsetDecoder charsetdecoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);

                    properties = new Properties();
                    properties.load(new InputStreamReader(inputstream, charsetdecoder));
                    properties1 = properties;
                } catch (Throwable throwable) {
                    if (inputstream != null) {
                        try {
                            inputstream.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    }

                    throw throwable;
                }

                if (inputstream != null) {
                    inputstream.close();
                }

                return properties1;
            } catch (CharacterCodingException charactercodingexception) {
                PropertyManager.LOGGER.info("Failed to load properties as UTF-8 from file {}, trying ISO_8859_1", path);
                BufferedReader bufferedreader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1);

                try {
                    properties = new Properties();
                    properties.load(bufferedreader);
                    properties1 = properties;
                } catch (Throwable throwable2) {
                    if (bufferedreader != null) {
                        try {
                            bufferedreader.close();
                        } catch (Throwable throwable3) {
                            throwable2.addSuppressed(throwable3);
                        }
                    }

                    throw throwable2;
                }

                if (bufferedreader != null) {
                    bufferedreader.close();
                }

                return properties1;
            }
        } catch (IOException ioexception) {
            PropertyManager.LOGGER.error("Failed to load properties from file: {}", path, ioexception);
            return new Properties();
        }
    }

    public void store(Path path) {
        try {
            // CraftBukkit start - Don't attempt writing to file if it's read only
            if (path.toFile().exists() && !path.toFile().canWrite()) {
                return;
            }
            // CraftBukkit end
            BufferedWriter bufferedwriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

            try {
                this.properties.store(bufferedwriter, "Minecraft server properties");
            } catch (Throwable throwable) {
                if (bufferedwriter != null) {
                    try {
                        bufferedwriter.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                }

                throw throwable;
            }

            if (bufferedwriter != null) {
                bufferedwriter.close();
            }
        } catch (IOException ioexception) {
            PropertyManager.LOGGER.error("Failed to store properties to file: {}", path);
        }

    }

    private static <V extends Number> Function<String, V> wrapNumberDeserializer(Function<String, V> function) {
        return (s) -> {
            try {
                return (V) function.apply(s); // CraftBukkit - decompile error
            } catch (NumberFormatException numberformatexception) {
                return null;
            }
        };
    }

    protected static <V> Function<String, V> dispatchNumberOrString(IntFunction<V> intfunction, Function<String, V> function) {
        return (s) -> {
            try {
                return intfunction.apply(Integer.parseInt(s));
            } catch (NumberFormatException numberformatexception) {
                return function.apply(s);
            }
        };
    }

    @Nullable
    private String getStringRaw(String s) {
        return (String) getOverride(s, this.properties.getProperty(s)); // CraftBukkit
    }

    @Nullable
    protected <V> V getLegacy(String s, Function<String, V> function) {
        String s1 = this.getStringRaw(s);

        if (s1 == null) {
            return null;
        } else {
            this.properties.remove(s);
            return function.apply(s1);
        }
    }

    protected <V> V get(String s, Function<String, V> function, Function<V, String> function1, V v0) {
        // CraftBukkit start
        try {
            return get0(s, function, function1, v0);
        } catch (Exception ex) {
            throw new RuntimeException("Could not load invalidly configured property '" + s + "'", ex);
        }
    }

    private <V> V get0(String s, Function<String, V> function, Function<V, String> function1, V v0) {
        // CraftBukkit end
        String s1 = this.getStringRaw(s);
        V v1 = MoreObjects.firstNonNull(s1 != null ? function.apply(s1) : null, v0);

        this.properties.put(s, function1.apply(v1));
        return v1;
    }

    protected <V> PropertyManager<T>.EditableProperty<V> getMutable(String s, Function<String, V> function, Function<V, String> function1, V v0) {
        String s1 = this.getStringRaw(s);
        V v1 = MoreObjects.firstNonNull(s1 != null ? function.apply(s1) : null, v0);

        this.properties.put(s, function1.apply(v1));
        return new PropertyManager.EditableProperty(s, v1, function1); // CraftBukkit - decompile error
    }

    protected <V> V get(String s, Function<String, V> function, UnaryOperator<V> unaryoperator, Function<V, String> function1, V v0) {
        return this.get(s, (s1) -> {
            V v1 = function.apply(s1);

            return v1 != null ? unaryoperator.apply(v1) : null;
        }, function1, v0);
    }

    protected <V> V get(String s, Function<String, V> function, V v0) {
        return this.get(s, function, Objects::toString, v0);
    }

    protected <V> PropertyManager<T>.EditableProperty<V> getMutable(String s, Function<String, V> function, V v0) {
        return this.getMutable(s, function, Objects::toString, v0);
    }

    protected String get(String s, String s1) {
        return (String) this.get(s, Function.identity(), Function.identity(), s1);
    }

    @Nullable
    protected String getLegacyString(String s) {
        return (String) this.getLegacy(s, Function.identity());
    }

    protected int get(String s, int i) {
        return (Integer) this.get(s, wrapNumberDeserializer(Integer::parseInt), i);
    }

    protected PropertyManager<T>.EditableProperty<Integer> getMutable(String s, int i) {
        return this.getMutable(s, wrapNumberDeserializer(Integer::parseInt), i);
    }

    protected int get(String s, UnaryOperator<Integer> unaryoperator, int i) {
        return (Integer) this.get(s, wrapNumberDeserializer(Integer::parseInt), unaryoperator, Objects::toString, i);
    }

    protected long get(String s, long i) {
        return (Long) this.get(s, wrapNumberDeserializer(Long::parseLong), i);
    }

    protected boolean get(String s, boolean flag) {
        return (Boolean) this.get(s, Boolean::valueOf, flag);
    }

    protected PropertyManager<T>.EditableProperty<Boolean> getMutable(String s, boolean flag) {
        return this.getMutable(s, Boolean::valueOf, flag);
    }

    @Nullable
    protected Boolean getLegacyBoolean(String s) {
        return (Boolean) this.getLegacy(s, Boolean::valueOf);
    }

    protected Properties cloneProperties() {
        Properties properties = new Properties();

        properties.putAll(this.properties);
        return properties;
    }

    protected abstract T reload(IRegistryCustom iregistrycustom, Properties properties, OptionSet optionset); // CraftBukkit

    public class EditableProperty<V> implements Supplier<V> {

        private final String key;
        private final V value;
        private final Function<V, String> serializer;

        EditableProperty(String s, V object, Function function) { // CraftBukkit - decompile error
            this.key = s;
            this.value = object;
            this.serializer = function;
        }

        public V get() {
            return this.value;
        }

        public T update(IRegistryCustom iregistrycustom, V v0) {
            Properties properties = PropertyManager.this.cloneProperties();

            properties.put(this.key, this.serializer.apply(v0));
            return PropertyManager.this.reload(iregistrycustom, properties, PropertyManager.this.options); // CraftBukkit
        }
    }
}
