package net.minecraft.advancements;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;

public class CriterionProgress {

    @Nullable
    private Instant obtained;

    public CriterionProgress() {}

    public CriterionProgress(Instant instant) {
        this.obtained = instant;
    }

    public boolean isDone() {
        return this.obtained != null;
    }

    public void grant() {
        this.obtained = Instant.now();
    }

    public void revoke() {
        this.obtained = null;
    }

    @Nullable
    public Instant getObtained() {
        return this.obtained;
    }

    public String toString() {
        Object object = this.obtained == null ? "false" : this.obtained;

        return "CriterionProgress{obtained=" + String.valueOf(object) + "}";
    }

    public void serializeToNetwork(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeNullable(this.obtained, PacketDataSerializer::writeInstant);
    }

    public static CriterionProgress fromNetwork(PacketDataSerializer packetdataserializer) {
        CriterionProgress criterionprogress = new CriterionProgress();

        criterionprogress.obtained = (Instant) packetdataserializer.readNullable(PacketDataSerializer::readInstant);
        return criterionprogress;
    }
}
