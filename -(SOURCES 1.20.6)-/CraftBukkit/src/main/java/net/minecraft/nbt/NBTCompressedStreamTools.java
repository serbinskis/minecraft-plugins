// mc-dev import
package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.SystemUtils;
import net.minecraft.util.DelegateDataOutput;
import net.minecraft.util.FastBufferedInputStream;

public class NBTCompressedStreamTools {

    private static final OpenOption[] SYNC_OUTPUT_OPTIONS = new OpenOption[]{StandardOpenOption.SYNC, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

    public NBTCompressedStreamTools() {}

    public static NBTTagCompound readCompressed(Path path, NBTReadLimiter nbtreadlimiter) throws IOException {
        InputStream inputstream = Files.newInputStream(path);

        NBTTagCompound nbttagcompound;

        try {
            FastBufferedInputStream fastbufferedinputstream = new FastBufferedInputStream(inputstream);

            try {
                nbttagcompound = readCompressed((InputStream) fastbufferedinputstream, nbtreadlimiter);
            } catch (Throwable throwable) {
                try {
                    fastbufferedinputstream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }

                throw throwable;
            }

            fastbufferedinputstream.close();
        } catch (Throwable throwable2) {
            if (inputstream != null) {
                try {
                    inputstream.close();
                } catch (Throwable throwable3) {
                    throwable2.addSuppressed(throwable3);
                }
            }

            throw throwable2;
        }

        if (inputstream != null) {
            inputstream.close();
        }

        return nbttagcompound;
    }

    private static DataInputStream createDecompressorStream(InputStream inputstream) throws IOException {
        return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputstream)));
    }

    private static DataOutputStream createCompressorStream(OutputStream outputstream) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputstream)));
    }

    public static NBTTagCompound readCompressed(InputStream inputstream, NBTReadLimiter nbtreadlimiter) throws IOException {
        DataInputStream datainputstream = createDecompressorStream(inputstream);

        NBTTagCompound nbttagcompound;

        try {
            nbttagcompound = read(datainputstream, nbtreadlimiter);
        } catch (Throwable throwable) {
            if (datainputstream != null) {
                try {
                    datainputstream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (datainputstream != null) {
            datainputstream.close();
        }

        return nbttagcompound;
    }

    public static void parseCompressed(Path path, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
        InputStream inputstream = Files.newInputStream(path);

        try {
            FastBufferedInputStream fastbufferedinputstream = new FastBufferedInputStream(inputstream);

            try {
                parseCompressed((InputStream) fastbufferedinputstream, streamtagvisitor, nbtreadlimiter);
            } catch (Throwable throwable) {
                try {
                    fastbufferedinputstream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }

                throw throwable;
            }

            fastbufferedinputstream.close();
        } catch (Throwable throwable2) {
            if (inputstream != null) {
                try {
                    inputstream.close();
                } catch (Throwable throwable3) {
                    throwable2.addSuppressed(throwable3);
                }
            }

            throw throwable2;
        }

        if (inputstream != null) {
            inputstream.close();
        }

    }

    public static void parseCompressed(InputStream inputstream, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
        DataInputStream datainputstream = createDecompressorStream(inputstream);

        try {
            parse(datainputstream, streamtagvisitor, nbtreadlimiter);
        } catch (Throwable throwable) {
            if (datainputstream != null) {
                try {
                    datainputstream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (datainputstream != null) {
            datainputstream.close();
        }

    }

    public static void writeCompressed(NBTTagCompound nbttagcompound, Path path) throws IOException {
        OutputStream outputstream = Files.newOutputStream(path, NBTCompressedStreamTools.SYNC_OUTPUT_OPTIONS);

        try {
            BufferedOutputStream bufferedoutputstream = new BufferedOutputStream(outputstream);

            try {
                writeCompressed(nbttagcompound, (OutputStream) bufferedoutputstream);
            } catch (Throwable throwable) {
                try {
                    bufferedoutputstream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }

                throw throwable;
            }

            bufferedoutputstream.close();
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

    }

    public static void writeCompressed(NBTTagCompound nbttagcompound, OutputStream outputstream) throws IOException {
        DataOutputStream dataoutputstream = createCompressorStream(outputstream);

        try {
            write(nbttagcompound, (DataOutput) dataoutputstream);
        } catch (Throwable throwable) {
            if (dataoutputstream != null) {
                try {
                    dataoutputstream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (dataoutputstream != null) {
            dataoutputstream.close();
        }

    }

    public static void write(NBTTagCompound nbttagcompound, Path path) throws IOException {
        OutputStream outputstream = Files.newOutputStream(path, NBTCompressedStreamTools.SYNC_OUTPUT_OPTIONS);

        try {
            BufferedOutputStream bufferedoutputstream = new BufferedOutputStream(outputstream);

            try {
                DataOutputStream dataoutputstream = new DataOutputStream(bufferedoutputstream);

                try {
                    write(nbttagcompound, (DataOutput) dataoutputstream);
                } catch (Throwable throwable) {
                    try {
                        dataoutputstream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }

                    throw throwable;
                }

                dataoutputstream.close();
            } catch (Throwable throwable2) {
                try {
                    bufferedoutputstream.close();
                } catch (Throwable throwable3) {
                    throwable2.addSuppressed(throwable3);
                }

                throw throwable2;
            }

            bufferedoutputstream.close();
        } catch (Throwable throwable4) {
            if (outputstream != null) {
                try {
                    outputstream.close();
                } catch (Throwable throwable5) {
                    throwable4.addSuppressed(throwable5);
                }
            }

            throw throwable4;
        }

        if (outputstream != null) {
            outputstream.close();
        }

    }

    @Nullable
    public static NBTTagCompound read(Path path) throws IOException {
        if (!Files.exists(path, new LinkOption[0])) {
            return null;
        } else {
            InputStream inputstream = Files.newInputStream(path);

            NBTTagCompound nbttagcompound;

            try {
                DataInputStream datainputstream = new DataInputStream(inputstream);

                try {
                    nbttagcompound = read(datainputstream, NBTReadLimiter.unlimitedHeap());
                } catch (Throwable throwable) {
                    try {
                        datainputstream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }

                    throw throwable;
                }

                datainputstream.close();
            } catch (Throwable throwable2) {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (Throwable throwable3) {
                        throwable2.addSuppressed(throwable3);
                    }
                }

                throw throwable2;
            }

            if (inputstream != null) {
                inputstream.close();
            }

            return nbttagcompound;
        }
    }

    public static NBTTagCompound read(DataInput datainput) throws IOException {
        return read(datainput, NBTReadLimiter.unlimitedHeap());
    }

    public static NBTTagCompound read(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        NBTBase nbtbase = readUnnamedTag(datainput, nbtreadlimiter);

        if (nbtbase instanceof NBTTagCompound) {
            return (NBTTagCompound) nbtbase;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(NBTTagCompound nbttagcompound, DataOutput dataoutput) throws IOException {
        writeUnnamedTagWithFallback(nbttagcompound, dataoutput);
    }

    public static void parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
        NBTTagType<?> nbttagtype = NBTTagTypes.getType(datainput.readByte());

        if (nbttagtype == NBTTagEnd.TYPE) {
            if (streamtagvisitor.visitRootEntry(NBTTagEnd.TYPE) == StreamTagVisitor.b.CONTINUE) {
                streamtagvisitor.visitEnd();
            }

        } else {
            switch (streamtagvisitor.visitRootEntry(nbttagtype)) {
                case HALT:
                default:
                    break;
                case BREAK:
                    NBTTagString.skipString(datainput);
                    nbttagtype.skip(datainput, nbtreadlimiter);
                    break;
                case CONTINUE:
                    NBTTagString.skipString(datainput);
                    nbttagtype.parse(datainput, streamtagvisitor, nbtreadlimiter);
            }

        }
    }

    public static NBTBase readAnyTag(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        byte b0 = datainput.readByte();

        return (NBTBase) (b0 == 0 ? NBTTagEnd.INSTANCE : readTagSafe(datainput, nbtreadlimiter, b0));
    }

    public static void writeAnyTag(NBTBase nbtbase, DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(nbtbase.getId());
        if (nbtbase.getId() != 0) {
            nbtbase.write(dataoutput);
        }
    }

    public static void writeUnnamedTag(NBTBase nbtbase, DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(nbtbase.getId());
        if (nbtbase.getId() != 0) {
            dataoutput.writeUTF("");
            nbtbase.write(dataoutput);
        }
    }

    public static void writeUnnamedTagWithFallback(NBTBase nbtbase, DataOutput dataoutput) throws IOException {
        writeUnnamedTag(nbtbase, new NBTCompressedStreamTools.a(dataoutput));
    }

    private static NBTBase readUnnamedTag(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        byte b0 = datainput.readByte();

        if (b0 == 0) {
            return NBTTagEnd.INSTANCE;
        } else {
            NBTTagString.skipString(datainput);
            return readTagSafe(datainput, nbtreadlimiter, b0);
        }
    }

    private static NBTBase readTagSafe(DataInput datainput, NBTReadLimiter nbtreadlimiter, byte b0) {
        try {
            return NBTTagTypes.getType(b0).load(datainput, nbtreadlimiter);
        } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("NBT Tag");

            crashreportsystemdetails.setDetail("Tag type", (Object) b0);
            throw new ReportedNbtException(crashreport);
        }
    }

    public static class a extends DelegateDataOutput {

        public a(DataOutput dataoutput) {
            super(dataoutput);
        }

        @Override
        public void writeUTF(String s) throws IOException {
            try {
                super.writeUTF(s);
            } catch (UTFDataFormatException utfdataformatexception) {
                SystemUtils.logAndPauseIfInIde("Failed to write NBT String", utfdataformatexception);
                super.writeUTF("");
            }

        }
    }
}
