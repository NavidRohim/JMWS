package me.brynview.navidrohim.jmws.plugin;

import commonnetwork.networking.data.PacketContext;
import me.brynview.navidrohim.jmws.CommonClass;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helpers.JMWSSounds;
import me.brynview.navidrohim.jmws.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.platform.Services;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.network.chat.Component;

import java.util.Objects;

import static me.brynview.navidrohim.jmws.helper.PlayerHelper.sendUserAlert;

public class PacketHandler {
    public static void handlePacket(PacketContext<JMWSActionPayload> Context) {
        JMWSActionPayload waypointPayload = Context.message();

        if (CommonClass.getEnabledStatus()) {

            if (CommonClass.minecraftClientInstance.player == null)
            {
                return;
            }

            switch (waypointPayload.command()) {

                // Was creation_response
                // Sends no outbound data
                case SYNC -> JMWSPlugin.syncHandler(waypointPayload, CommonClass.minecraftClientInstance.player);

                // was "update"
                // Sends "request" packet | New = "SYNC"
                case REQUEST_CLIENT_SYNC -> JMWSPlugin.updateWaypoints(true);

                // was display_interval
                // No outbound data
                case COMMON_DISPLAY_INTERVAL -> sendUserAlert(Component.translatable("message.jmws.sync_frequency", CommonClass.getSyncFrequency()), true, false, JMWSMessageType.NEUTRAL);

                // was "alert"
                // No outbound data
                case CLIENT_ALERT -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    boolean isError = waypointPayload.arguments().getLast().getAsBoolean();
                    JMWSMessageType messageType = JMWSMessageType.NEUTRAL;

                    if (isError) {
                        messageType = JMWSMessageType.FAILURE;
                        PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                    }

                    sendUserAlert(Component.translatable(firstArgument), waypointPayload.arguments().get(1).getAsBoolean(), false, messageType);
                }

                // was "deleteWaypoint"
                // No outbound data
                case COMMON_DELETE_WAYPOINT ->
                {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    JMWSPlugin.getInstance().deleteSavedObjects(
                            Objects.equals(firstArgument, "*"),
                            JMWSServerIO.FetchType.valueOf(waypointPayload.arguments().get(1).getAsString()),
                            firstArgument
                    );
                }

                // was "display_next_update"
                // No outbound data
                case COMMON_DISPLAY_NEXT_UPDATE -> sendUserAlert(Component.translatable("message.jmws.next_sync", (CommonClass.timeUntilNextSync())), true, false, JMWSMessageType.NEUTRAL);

                default -> Constants.getLogger().warn("Unknown packet command -> " + waypointPayload.command());
            }
        }
    }

    public static void HandshakeHandler(JMWSHandshakePayload _handshakePayload) {
        sendUserAlert(Component.translatable("message.jmws.has_jmws"), true, false, JMWSMessageType.SUCCESS);
        Services.PLATFORM.setServerModStatus(true);
    }
}
