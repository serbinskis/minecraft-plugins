package net.minecraft.network.codec;

@FunctionalInterface
public interface StreamMemberEncoder<O, T> {

    void encode(T t0, O o0);
}
