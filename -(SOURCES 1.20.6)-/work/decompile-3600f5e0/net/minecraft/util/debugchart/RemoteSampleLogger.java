package net.minecraft.util.debugchart;

import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;

public class RemoteSampleLogger extends AbstractSampleLogger {

    private final DebugSampleSubscriptionTracker subscriptionTracker;
    private final RemoteDebugSampleType sampleType;

    public RemoteSampleLogger(int i, DebugSampleSubscriptionTracker debugsamplesubscriptiontracker, RemoteDebugSampleType remotedebugsampletype) {
        this(i, debugsamplesubscriptiontracker, remotedebugsampletype, new long[i]);
    }

    public RemoteSampleLogger(int i, DebugSampleSubscriptionTracker debugsamplesubscriptiontracker, RemoteDebugSampleType remotedebugsampletype, long[] along) {
        super(i, along);
        this.subscriptionTracker = debugsamplesubscriptiontracker;
        this.sampleType = remotedebugsampletype;
    }

    @Override
    protected void useSample() {
        this.subscriptionTracker.broadcast(new ClientboundDebugSamplePacket((long[]) this.sample.clone(), this.sampleType));
    }
}
