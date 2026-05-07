package me.brynview.navidrohim.jmws.paper;

import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.common.platform.services.IPlatformHelper;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.MessageTooLargeException;

import java.nio.file.Path;
import java.util.UUID;

public class PaperPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Paper";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return Bukkit.getPluginManager().isPluginEnabled(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !Boolean.getBoolean("jmws.production");
    }

    @Override
    public String side() {
        return "SERVER";
    }

    @Override
    public void sendActionPayloadToClient(JMWSActionPayload payload, UUID playerUuid) {
        byte[] encoded = payload.toByteArray();
        if (encoded.length <= Messenger.MAX_MESSAGE_SIZE) {
            sendPayload(playerUuid, JMWSActionPayload.CHANNEL, encoded);
            return;
        }

        JmwsPaperPlugin plugin = JmwsPaperPlugin.getInstance();
        if (plugin != null) {
            Player player = Bukkit.getPlayer(playerUuid);
            String playerName = player != null ? player.getName() : playerUuid.toString();
            plugin.getLogger().warning("Could not send JMWS action payload to " + playerName + " because it is too large for Paper plugin messaging.");
        }

        JMWSActionPayload alertPayload = new JMWSActionPayload(CommandFactory.makeClientAlertRequestJson("error.jmws.error_packet_size", true, MessageType.FAILURE));
        sendPayload(playerUuid, JMWSActionPayload.CHANNEL, alertPayload.toByteArray());
    }

    @Override
    public void sendHandshakePayloadToClient(JMWSHandshakePayload payload, UUID playerUuid) {
        sendPayload(playerUuid, JMWSHandshakePayload.CHANNEL, payload.toByteArray());
    }

    @Override
    public boolean isOperator(UUID playerUuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
        return player.isOp();
    }

    @Override
    public Path getServerDataDirectory() {
        JmwsPaperPlugin plugin = JmwsPaperPlugin.getInstance();
        return plugin != null ? plugin.getDataFolder().toPath() : Path.of("plugins", "JMWS");
    }

    @Override
    public Path getServerConfigPath() {
        return getServerDataDirectory().resolve("config.yml");
    }

    @Override
    public Path getWaypointDirectory() {
        return getServerDataDirectory().resolve("waypoints");
    }

    @Override
    public Path getGroupDirectory() {
        return getServerDataDirectory().resolve("groups");
    }

    @Override
    public Path getUserDirectory() {
        return getServerDataDirectory().resolve("users");
    }

    @Override
    public boolean shouldSendSyncAlert(boolean requestedByClient, boolean isDeathSync) {
        return requestedByClient && (isDeathSync || PaperServerOptions.syncAlertsEnabled());
    }

    private void sendPayload(UUID playerUuid, String channel, byte[] message) {
        JmwsPaperPlugin plugin = JmwsPaperPlugin.getInstance();
        Player player = Bukkit.getPlayer(playerUuid);
        if (plugin == null || player == null) {
            return;
        }

        try {
            player.sendPluginMessage(plugin, channel, message);
        } catch (MessageTooLargeException tooLarge) {
            plugin.getLogger().warning("Could not send JMWS payload on " + channel + " to " + player.getName() + " because it is too large for Paper plugin messaging.");
        }
    }

}
