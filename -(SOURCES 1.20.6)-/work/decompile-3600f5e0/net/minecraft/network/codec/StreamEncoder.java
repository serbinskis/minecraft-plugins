package net.minecraft.network.codec;

@FunctionalInterface
public interface StreamEncoder<O, T> {

    void encode(O o0, T t0);
}
