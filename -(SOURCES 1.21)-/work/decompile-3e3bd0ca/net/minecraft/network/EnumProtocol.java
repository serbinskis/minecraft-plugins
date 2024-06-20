package net.minecraft.network;

public enum EnumProtocol {

    HANDSHAKING("handshake"), PLAY("play"), STATUS("status"), LOGIN("login"), CONFIGURATION("configuration");

    private final String id;

    private EnumProtocol(final String s) {
        this.id = s;
    }

    public String id() {
        return this.id;
    }
}
