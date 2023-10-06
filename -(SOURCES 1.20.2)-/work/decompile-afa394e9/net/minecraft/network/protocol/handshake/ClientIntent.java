package net.minecraft.network.protocol.handshake;

import net.minecraft.network.EnumProtocol;

public enum ClientIntent {

    STATUS, LOGIN;

    private static final int STATUS_ID = 1;
    private static final int LOGIN_ID = 2;

    private ClientIntent() {}

    public static ClientIntent byId(int i) {
        ClientIntent clientintent;

        switch (i) {
            case 1:
                clientintent = ClientIntent.STATUS;
                break;
            case 2:
                clientintent = ClientIntent.LOGIN;
                break;
            default:
                throw new IllegalArgumentException("Unknown connection intent: " + i);
        }

        return clientintent;
    }

    public int id() {
        byte b0;

        switch (this) {
            case STATUS:
                b0 = 1;
                break;
            case LOGIN:
                b0 = 2;
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return b0;
    }

    public EnumProtocol protocol() {
        EnumProtocol enumprotocol;

        switch (this) {
            case STATUS:
                enumprotocol = EnumProtocol.STATUS;
                break;
            case LOGIN:
                enumprotocol = EnumProtocol.LOGIN;
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return enumprotocol;
    }
}
