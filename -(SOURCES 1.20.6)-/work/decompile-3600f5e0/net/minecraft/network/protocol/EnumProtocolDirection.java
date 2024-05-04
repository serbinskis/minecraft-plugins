package net.minecraft.network.protocol;

public enum EnumProtocolDirection {

    SERVERBOUND("serverbound"), CLIENTBOUND("clientbound");

    private final String id;

    private EnumProtocolDirection(final String s) {
        this.id = s;
    }

    public EnumProtocolDirection getOpposite() {
        return this == EnumProtocolDirection.CLIENTBOUND ? EnumProtocolDirection.SERVERBOUND : EnumProtocolDirection.CLIENTBOUND;
    }

    public String id() {
        return this.id;
    }
}
