package net.minecraft.network.protocol.handshake;

public enum ClientIntent {

    STATUS, LOGIN, TRANSFER;

    private static final int STATUS_ID = 1;
    private static final int LOGIN_ID = 2;
    private static final int TRANSFER_ID = 3;

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
            case 3:
                clientintent = ClientIntent.TRANSFER;
                break;
            default:
                throw new IllegalArgumentException("Unknown connection intent: " + i);
        }

        return clientintent;
    }

    public int id() {
        byte b0;

        switch (this.ordinal()) {
            case 0:
                b0 = 1;
                break;
            case 1:
                b0 = 2;
                break;
            case 2:
                b0 = 3;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return b0;
    }
}
