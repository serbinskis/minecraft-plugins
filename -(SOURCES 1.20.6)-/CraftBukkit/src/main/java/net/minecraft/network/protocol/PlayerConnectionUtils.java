package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.server.CancelledPacketHandleException;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.thread.IAsyncTaskHandler;
import org.slf4j.Logger;

// CraftBukkit start
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
// CraftBukkit end

public class PlayerConnectionUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    public PlayerConnectionUtils() {}

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T t0, WorldServer worldserver) throws CancelledPacketHandleException {
        ensureRunningOnSameThread(packet, t0, (IAsyncTaskHandler) worldserver.getServer());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T t0, IAsyncTaskHandler<?> iasynctaskhandler) throws CancelledPacketHandleException {
        if (!iasynctaskhandler.isSameThread()) {
            iasynctaskhandler.executeIfPossible(() -> {
                if (t0 instanceof ServerCommonPacketListenerImpl serverCommonPacketListener && serverCommonPacketListener.processedDisconnect) return; // CraftBukkit - Don't handle sync packets for kicked players
                if (t0.shouldHandleMessage(packet)) {
                    try {
                        packet.handle(t0);
                    } catch (Exception exception) {
                        if (exception instanceof ReportedException) {
                            ReportedException reportedexception = (ReportedException) exception;

                            if (reportedexception.getCause() instanceof OutOfMemoryError) {
                                throw makeReportedException(exception, packet, t0);
                            }
                        }

                        t0.onPacketError(packet, exception);
                    }
                } else {
                    PlayerConnectionUtils.LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
                }

            });
            throw CancelledPacketHandleException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }

    public static <T extends PacketListener> ReportedException makeReportedException(Exception exception, Packet<T> packet, T t0) {
        if (exception instanceof ReportedException reportedexception) {
            fillCrashReport(reportedexception.getReport(), t0, packet);
            return reportedexception;
        } else {
            CrashReport crashreport = CrashReport.forThrowable(exception, "Main thread packet handler");

            fillCrashReport(crashreport, t0, packet);
            return new ReportedException(crashreport);
        }
    }

    private static <T extends PacketListener> void fillCrashReport(CrashReport crashreport, T t0, Packet<T> packet) {
        CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Incoming Packet");

        crashreportsystemdetails.setDetail("Type", () -> {
            return packet.type().toString();
        });
        crashreportsystemdetails.setDetail("Is Terminal", () -> {
            return Boolean.toString(packet.isTerminal());
        });
        crashreportsystemdetails.setDetail("Is Skippable", () -> {
            return Boolean.toString(packet.isSkippable());
        });
        t0.fillCrashReport(crashreport);
    }
}
