package net.minecraft.util;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import javax.annotation.Nullable;
import net.minecraft.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class HttpUtilities {

    private static final Logger LOGGER = LogUtils.getLogger();

    private HttpUtilities() {}

    public static Path downloadFile(Path path, URL url, Map<String, String> map, HashFunction hashfunction, @Nullable HashCode hashcode, int i, Proxy proxy, HttpUtilities.a httputilities_a) {
        HttpURLConnection httpurlconnection = null;
        InputStream inputstream = null;

        httputilities_a.requestStart();
        Path path1;

        if (hashcode != null) {
            path1 = cachedFilePath(path, hashcode);

            try {
                if (checkExistingFile(path1, hashfunction, hashcode)) {
                    HttpUtilities.LOGGER.info("Returning cached file since actual hash matches requested");
                    httputilities_a.requestFinished(true);
                    updateModificationTime(path1);
                    return path1;
                }
            } catch (IOException ioexception) {
                HttpUtilities.LOGGER.warn("Failed to check cached file {}", path1, ioexception);
            }

            try {
                HttpUtilities.LOGGER.warn("Existing file {} not found or had mismatched hash", path1);
                Files.deleteIfExists(path1);
            } catch (IOException ioexception1) {
                httputilities_a.requestFinished(false);
                throw new UncheckedIOException("Failed to remove existing file " + String.valueOf(path1), ioexception1);
            }
        } else {
            path1 = null;
        }

        Path path2;

        try {
            httpurlconnection = (HttpURLConnection) url.openConnection(proxy);
            httpurlconnection.setInstanceFollowRedirects(true);
            Objects.requireNonNull(httpurlconnection);
            map.forEach(httpurlconnection::setRequestProperty);
            inputstream = httpurlconnection.getInputStream();
            long j = httpurlconnection.getContentLengthLong();
            OptionalLong optionallong = j != -1L ? OptionalLong.of(j) : OptionalLong.empty();

            FileUtils.createDirectoriesSafe(path);
            httputilities_a.downloadStart(optionallong);
            String s;

            if (optionallong.isPresent() && optionallong.getAsLong() > (long) i) {
                s = String.valueOf(optionallong);
                throw new IOException("Filesize is bigger than maximum allowed (file is " + s + ", limit is " + i + ")");
            }

            if (path1 == null) {
                Path path3 = Files.createTempFile(path, "download", ".tmp");

                try {
                    HashCode hashcode1 = downloadAndHash(hashfunction, i, httputilities_a, inputstream, path3);
                    Path path4 = cachedFilePath(path, hashcode1);

                    if (!checkExistingFile(path4, hashfunction, hashcode1)) {
                        Files.move(path3, path4, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        updateModificationTime(path4);
                    }

                    httputilities_a.requestFinished(true);
                    Path path5 = path4;

                    return path5;
                } finally {
                    Files.deleteIfExists(path3);
                }
            }

            HashCode hashcode2 = downloadAndHash(hashfunction, i, httputilities_a, inputstream, path1);

            if (!hashcode2.equals(hashcode)) {
                s = String.valueOf(hashcode2);
                throw new IOException("Hash of downloaded file (" + s + ") did not match requested (" + String.valueOf(hashcode) + ")");
            }

            httputilities_a.requestFinished(true);
            path2 = path1;
        } catch (Throwable throwable) {
            if (httpurlconnection != null) {
                InputStream inputstream1 = httpurlconnection.getErrorStream();

                if (inputstream1 != null) {
                    try {
                        HttpUtilities.LOGGER.error("HTTP response error: {}", IOUtils.toString(inputstream1, StandardCharsets.UTF_8));
                    } catch (Exception exception) {
                        HttpUtilities.LOGGER.error("Failed to read response from server");
                    }
                }
            }

            httputilities_a.requestFinished(false);
            throw new IllegalStateException("Failed to download file " + String.valueOf(url), throwable);
        } finally {
            IOUtils.closeQuietly(inputstream);
        }

        return path2;
    }

    private static void updateModificationTime(Path path) {
        try {
            Files.setLastModifiedTime(path, FileTime.from(Instant.now()));
        } catch (IOException ioexception) {
            HttpUtilities.LOGGER.warn("Failed to update modification time of {}", path, ioexception);
        }

    }

    private static HashCode hashFile(Path path, HashFunction hashfunction) throws IOException {
        Hasher hasher = hashfunction.newHasher();
        OutputStream outputstream = Funnels.asOutputStream(hasher);

        try {
            InputStream inputstream = Files.newInputStream(path);

            try {
                inputstream.transferTo(outputstream);
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
        } catch (Throwable throwable2) {
            if (outputstream != null) {
                try {
                    outputstream.close();
                } catch (Throwable throwable3) {
                    throwable2.addSuppressed(throwable3);
                }
            }

            throw throwable2;
        }

        if (outputstream != null) {
            outputstream.close();
        }

        return hasher.hash();
    }

    private static boolean checkExistingFile(Path path, HashFunction hashfunction, HashCode hashcode) throws IOException {
        if (Files.exists(path, new LinkOption[0])) {
            HashCode hashcode1 = hashFile(path, hashfunction);

            if (hashcode1.equals(hashcode)) {
                return true;
            }

            HttpUtilities.LOGGER.warn("Mismatched hash of file {}, expected {} but found {}", new Object[]{path, hashcode, hashcode1});
        }

        return false;
    }

    private static Path cachedFilePath(Path path, HashCode hashcode) {
        return path.resolve(hashcode.toString());
    }

    private static HashCode downloadAndHash(HashFunction hashfunction, int i, HttpUtilities.a httputilities_a, InputStream inputstream, Path path) throws IOException {
        OutputStream outputstream = Files.newOutputStream(path, StandardOpenOption.CREATE);

        HashCode hashcode;

        try {
            Hasher hasher = hashfunction.newHasher();
            byte[] abyte = new byte[8196];
            long j = 0L;

            int k;

            while ((k = inputstream.read(abyte)) >= 0) {
                j += (long) k;
                httputilities_a.downloadedBytes(j);
                if (j > (long) i) {
                    throw new IOException("Filesize was bigger than maximum allowed (got >= " + j + ", limit was " + i + ")");
                }

                if (Thread.interrupted()) {
                    HttpUtilities.LOGGER.error("INTERRUPTED");
                    throw new IOException("Download interrupted");
                }

                outputstream.write(abyte, 0, k);
                hasher.putBytes(abyte, 0, k);
            }

            hashcode = hasher.hash();
        } catch (Throwable throwable) {
            if (outputstream != null) {
                try {
                    outputstream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (outputstream != null) {
            outputstream.close();
        }

        return hashcode;
    }

    public static int getAvailablePort() {
        try {
            ServerSocket serversocket = new ServerSocket(0);

            int i;

            try {
                i = serversocket.getLocalPort();
            } catch (Throwable throwable) {
                try {
                    serversocket.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }

                throw throwable;
            }

            serversocket.close();
            return i;
        } catch (IOException ioexception) {
            return 25564;
        }
    }

    public static boolean isPortAvailable(int i) {
        if (i >= 0 && i <= 65535) {
            try {
                ServerSocket serversocket = new ServerSocket(i);

                boolean flag;

                try {
                    flag = serversocket.getLocalPort() == i;
                } catch (Throwable throwable) {
                    try {
                        serversocket.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }

                    throw throwable;
                }

                serversocket.close();
                return flag;
            } catch (IOException ioexception) {
                return false;
            }
        } else {
            return false;
        }
    }

    public interface a {

        void requestStart();

        void downloadStart(OptionalLong optionallong);

        void downloadedBytes(long i);

        void requestFinished(boolean flag);
    }
}
