package net.minecraft.world.level.validation;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class PathAllowList implements PathMatcher {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String COMMENT_PREFIX = "#";
    private final List<PathAllowList.a> entries;
    private final Map<String, PathMatcher> compiledPaths = new ConcurrentHashMap();

    public PathAllowList(List<PathAllowList.a> list) {
        this.entries = list;
    }

    public PathMatcher getForFileSystem(FileSystem filesystem) {
        return (PathMatcher) this.compiledPaths.computeIfAbsent(filesystem.provider().getScheme(), (s) -> {
            List list;

            try {
                list = this.entries.stream().map((pathallowlist_a) -> {
                    return pathallowlist_a.compile(filesystem);
                }).toList();
            } catch (Exception exception) {
                PathAllowList.LOGGER.error("Failed to compile file pattern list", exception);
                return (path) -> {
                    return false;
                };
            }

            PathMatcher pathmatcher;

            switch (list.size()) {
                case 0:
                    pathmatcher = (path) -> {
                        return false;
                    };
                    break;
                case 1:
                    pathmatcher = (PathMatcher) list.get(0);
                    break;
                default:
                    pathmatcher = (path) -> {
                        Iterator iterator = list.iterator();

                        PathMatcher pathmatcher1;

                        do {
                            if (!iterator.hasNext()) {
                                return false;
                            }

                            pathmatcher1 = (PathMatcher) iterator.next();
                        } while (!pathmatcher1.matches(path));

                        return true;
                    };
            }

            return pathmatcher;
        });
    }

    public boolean matches(Path path) {
        return this.getForFileSystem(path.getFileSystem()).matches(path);
    }

    public static PathAllowList readPlain(BufferedReader bufferedreader) {
        return new PathAllowList(bufferedreader.lines().flatMap((s) -> {
            return PathAllowList.a.parse(s).stream();
        }).toList());
    }

    public static record a(PathAllowList.b type, String pattern) {

        public PathMatcher compile(FileSystem filesystem) {
            return this.type().compile(filesystem, this.pattern);
        }

        static Optional<PathAllowList.a> parse(String s) {
            if (!s.isBlank() && !s.startsWith("#")) {
                if (!s.startsWith("[")) {
                    return Optional.of(new PathAllowList.a(PathAllowList.b.PREFIX, s));
                } else {
                    int i = s.indexOf(93, 1);

                    if (i == -1) {
                        throw new IllegalArgumentException("Unterminated type in line '" + s + "'");
                    } else {
                        String s1 = s.substring(1, i);
                        String s2 = s.substring(i + 1);
                        Optional optional;

                        switch (s1) {
                            case "glob":
                            case "regex":
                                optional = Optional.of(new PathAllowList.a(PathAllowList.b.FILESYSTEM, s1 + ":" + s2));
                                break;
                            case "prefix":
                                optional = Optional.of(new PathAllowList.a(PathAllowList.b.PREFIX, s2));
                                break;
                            default:
                                throw new IllegalArgumentException("Unsupported definition type in line '" + s + "'");
                        }

                        return optional;
                    }
                }
            } else {
                return Optional.empty();
            }
        }

        static PathAllowList.a glob(String s) {
            return new PathAllowList.a(PathAllowList.b.FILESYSTEM, "glob:" + s);
        }

        static PathAllowList.a regex(String s) {
            return new PathAllowList.a(PathAllowList.b.FILESYSTEM, "regex:" + s);
        }

        static PathAllowList.a prefix(String s) {
            return new PathAllowList.a(PathAllowList.b.PREFIX, s);
        }
    }

    @FunctionalInterface
    public interface b {

        PathAllowList.b FILESYSTEM = FileSystem::getPathMatcher;
        PathAllowList.b PREFIX = (filesystem, s) -> {
            return (path) -> {
                return path.toString().startsWith(s);
            };
        };

        PathMatcher compile(FileSystem filesystem, String s);
    }
}
