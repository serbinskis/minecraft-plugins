package net.minecraft.util;

import java.io.DataOutput;
import java.io.IOException;

public class DelegateDataOutput implements DataOutput {

    private final DataOutput parent;

    public DelegateDataOutput(DataOutput dataoutput) {
        this.parent = dataoutput;
    }

    public void write(int i) throws IOException {
        this.parent.write(i);
    }

    public void write(byte[] abyte) throws IOException {
        this.parent.write(abyte);
    }

    public void write(byte[] abyte, int i, int j) throws IOException {
        this.parent.write(abyte, i, j);
    }

    public void writeBoolean(boolean flag) throws IOException {
        this.parent.writeBoolean(flag);
    }

    public void writeByte(int i) throws IOException {
        this.parent.writeByte(i);
    }

    public void writeShort(int i) throws IOException {
        this.parent.writeShort(i);
    }

    public void writeChar(int i) throws IOException {
        this.parent.writeChar(i);
    }

    public void writeInt(int i) throws IOException {
        this.parent.writeInt(i);
    }

    public void writeLong(long i) throws IOException {
        this.parent.writeLong(i);
    }

    public void writeFloat(float f) throws IOException {
        this.parent.writeFloat(f);
    }

    public void writeDouble(double d0) throws IOException {
        this.parent.writeDouble(d0);
    }

    public void writeBytes(String s) throws IOException {
        this.parent.writeBytes(s);
    }

    public void writeChars(String s) throws IOException {
        this.parent.writeChars(s);
    }

    public void writeUTF(String s) throws IOException {
        this.parent.writeUTF(s);
    }
}
