package net.minecraft;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

public class FileUtils {

    private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_FILE_NAME = 255;
    private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
    private static final Pattern STRICT_PATH_SEGMENT_CHECK = Pattern.compile("[-._a-z0-9]+");

    public FileUtils() {}

    public static String sanitizeName(String s) {
        char[] achar = SharedConstants.ILLEGAL_FILE_CHARACTERS;
        int i = achar.length;

        for (int j = 0; j < i; ++j) {
            char c0 = achar[j];

            s = s.replace(c0, '_');
        }

        return s.replaceAll("[./\"]", "_");
    }

    public static String findAvailableName(Path path, String s, String s1) throws IOException {
        s = sanitizeName(s);
        if (FileUtils.RESERVED_WINDOWS_FILENAMES.matcher(s).matches()) {
            s = "_" + s + "_";
        }

        Matcher matcher = FileUtils.COPY_COUNTER_PATTERN.matcher(s);
        int i = 0;

        if (matcher.matches()) {
            s = matcher.group("name");
            i = Integer.parseInt(matcher.group("count"));
        }

        if (s.length() > 255 - s1.length()) {
            s = s.substring(0, 255 - s1.length());
        }

        while (true) {
            String s2 = s;

            if (i != 0) {
                String s3 = " (" + i + ")";
                int j = 255 - s3.length();

                if (s.length() > j) {
                    s2 = s.substring(0, j);
                }

                s2 = s2 + s3;
            }

            s2 = s2 + s1;
            Path path1 = path.resolve(s2);

            try {
                Path path2 = Files.createDirectory(path1);

                Files.deleteIfExists(path2);
                return path.relativize(path2).toString();
            } catch (FileAlreadyExistsException filealreadyexistsexception) {
                ++i;
            }
        }
    }

    public static boolean isPathNormalized(Path path) {
        Path path1 = path.normalize();

        return path1.equals(path);
    }

    public static boolean isPathPortable(Path path) {
        Iterator iterator = path.iterator();

        Path path1;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            path1 = (Path) iterator.next();
        } while (!FileUtils.RESERVED_WINDOWS_FILENAMES.matcher(path1.toString()).matches());

        return false;
    }

    public static Path createPathToResource(Path path, String s, String s1) {
        String s2 = s + s1;
        Path path1 = Paths.get(s2);

        if (path1.endsWith(s1)) {
            throw new InvalidPathException(s2, "empty resource name");
        } else {
            return path.resolve(path1);
        }
    }

    public static String getFullResourcePath(String s) {
        return FilenameUtils.getFullPath(s).replace(File.separator, "/");
    }

    public static String normalizeResourcePath(String s) {
        return FilenameUtils.normalize(s).replace(File.separator, "/");
    }

    public static DataResult<List<String>> decomposePath(String s) {
        int i = s.indexOf(47);

        if (i == -1) {
            DataResult dataresult;

            switch (s) {
                case "":
                case ".":
                case "..":
                    dataresult = DataResult.error(() -> {
                        return "Invalid path '" + s + "'";
                    });
                    break;
                default:
                    dataresult = !isValidStrictPathSegment(s) ? DataResult.error(() -> {
                        return "Invalid path '" + s + "'";
                    }) : DataResult.success(List.of(s));
            }

            return dataresult;
        } else {
            List<String> list = new ArrayList();
            int j = 0;
            boolean flag = false;

            while (true) {
                switch (s.substring(j, i)) {
                    case "":
                    case ".":
                    case "..":
                        return DataResult.error(() -> {
                            return "Invalid segment '" + s1 + "' in path '" + s + "'";
                        });
                }

                if (!isValidStrictPathSegment(s1)) {
                    return DataResult.error(() -> {
                        return "Invalid segment '" + s1 + "' in path '" + s + "'";
                    });
                }

                list.add(s1);
                if (flag) {
                    return DataResult.success(list);
                }

                j = i + 1;
                i = s.indexOf(47, j);
                if (i == -1) {
                    i = s.length();
                    flag = true;
                }
            }
        }
    }

    public static Path resolvePath(Path path, List<String> list) {
        int i = list.size();
        Path path1;

        switch (i) {
            case 0:
                path1 = path;
                break;
            case 1:
                path1 = path.resolve((String) list.get(0));
                break;
            default:
                String[] astring = new String[i - 1];

                for (int j = 1; j < i; ++j) {
                    astring[j - 1] = (String) list.get(j);
                }

                path1 = path.resolve(path.getFileSystem().getPath((String) list.get(0), astring));
        }

        return path1;
    }

    public static boolean isValidStrictPathSegment(String s) {
        return FileUtils.STRICT_PATH_SEGMENT_CHECK.matcher(s).matches();
    }

    public static void validatePath(String... astring) {
        if (astring.length == 0) {
            throw new IllegalArgumentException("Path must have at least one element");
        } else {
            String[] astring1 = astring;
            int i = astring.length;

            for (int j = 0; j < i; ++j) {
                String s = astring1[j];

                if (s.equals("..") || s.equals(".") || !isValidStrictPathSegment(s)) {
                    throw new IllegalArgumentException("Illegal segment " + s + " in path " + Arrays.toString(astring));
                }
            }

        }
    }

    public static void createDirectoriesSafe(Path path) throws IOException {
        Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
    }
}
