package net.minecraft.network;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.network.chat.IChatBaseComponent;

public record DisconnectionDetails(IChatBaseComponent reason, Optional<Path> report, Optional<URI> bugReportLink) {

    public DisconnectionDetails(IChatBaseComponent ichatbasecomponent) {
        this(ichatbasecomponent, Optional.empty(), Optional.empty());
    }
}
