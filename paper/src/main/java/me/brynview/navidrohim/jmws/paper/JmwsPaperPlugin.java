package me.brynview.navidrohim.jmws.paper;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.common.platform.Services;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.network.ServerNetworkDispatcher;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JmwsPaperPlugin extends JavaPlugin implements Listener, PluginMessageListener {
    private static final long[] HANDSHAKE_RETRY_OFFSETS = {0L, 40L, 100L, 200L, 400L};
    private static final long NO_TRAFFIC_WARNING_DELAY = 600L;

    private static JmwsPaperPlugin instance;
    private final Set<UUID> clientsWithActionTraffic = ConcurrentHashMap.newKeySet();
    private final Set<UUID> clientsWithHandshakeRequests = ConcurrentHashMap.newKeySet();
    private final Set<UUID> clientsWithLoggedHandshakeSend = ConcurrentHashMap.newKeySet();

    public static JmwsPaperPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        Services.PLATFORM.getPlatformName();
        PaperServerOptions.reload();
        CommonClass.init();
        registerPluginChannels();
        registerCommands();
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("JMWS Paper adapter enabled for client mod protocol " + Constants.SERVER_VERSION + ".");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        CommonClass.scheduler.shutdownNow();
        instance = null;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try {
            if (JMWSActionPayload.CHANNEL.equals(channel)) {
                JMWSActionPayload payload = new JMWSActionPayload(message);
                if (clientsWithActionTraffic.add(player.getUniqueId())) {
                    getLogger().info("Received JMWS client payload from " + player.getName() + " (" + payload.command() + ").");
                }
                ServerPacketHandler.handleIncomingActionCommand(payload, player.getUniqueId());
            } else if (JMWSHandshakePayload.CHANNEL.equals(channel)) {
                if (clientsWithHandshakeRequests.add(player.getUniqueId())) {
                    getLogger().info("Received JMWS handshake request from " + player.getName() + ".");
                }
                PlayerNetworkingHelper.sendHandshakeAndValidate(player.getUniqueId());
            }
        } catch (IllegalArgumentException error) {
            getLogger().warning("Ignored malformed JMWS payload from " + player.getName() + " on " + channel + ": " + error.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        long delayTicks = Math.max(1L, Math.round(ServerConfig.serverConfig.handshakeDelay / 50.0D));
        UUID playerUuid = event.getPlayer().getUniqueId();

        for (long offset : HANDSHAKE_RETRY_OFFSETS) {
            getServer().getScheduler().runTaskLater(this, () -> sendHandshakeIfOnline(playerUuid), delayTicks + offset);
        }

        getServer().getScheduler().runTaskLater(this, () -> warnIfNoClientTraffic(playerUuid), delayTicks + NO_TRAFFIC_WARNING_DELAY);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        if (PaperServerOptions.syncOnQuitEnabled()) {
            ServerNetworkDispatcher.requestClientSync(playerUuid);
        }
        clientsWithActionTraffic.remove(playerUuid);
        clientsWithHandshakeRequests.remove(playerUuid);
        clientsWithLoggedHandshakeSend.remove(playerUuid);
    }

    private void registerPluginChannels() {
        getServer().getMessenger().registerIncomingPluginChannel(this, JMWSActionPayload.CHANNEL, this);
        getServer().getMessenger().registerIncomingPluginChannel(this, JMWSHandshakePayload.CHANNEL, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, JMWSActionPayload.CHANNEL);
        getServer().getMessenger().registerOutgoingPluginChannel(this, JMWSHandshakePayload.CHANNEL);
    }

    private void registerCommands() {
        PaperCommandHandler handler = new PaperCommandHandler();
        registerCommand("share_waypoint", handler);
        registerCommand("share_group", handler);
        registerCommand("stop_sharing_waypoint", handler);
        registerCommand("stop_sharing_group", handler);
        registerCommand("jmws_handshake", handler);
        registerCommand("jmws_admin", handler);
        registerCommand("jmws", handler);
    }

    private void registerCommand(String name, PaperCommandHandler handler) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command missing from plugin.yml: " + name);
            return;
        }

        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }

    private void sendHandshakeIfOnline(UUID playerUuid) {
        Player player = getServer().getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        if (clientsWithLoggedHandshakeSend.add(playerUuid)) {
            getLogger().info("Sending JMWS handshake to " + player.getName() + " on " + JMWSHandshakePayload.CHANNEL + ". Client listening channels: " + player.getListeningPluginChannels());
        }
        PlayerNetworkingHelper.sendHandshakeAndValidate(playerUuid);
    }

    private void warnIfNoClientTraffic(UUID playerUuid) {
        Player player = getServer().getPlayer(playerUuid);
        if (player != null && player.isOnline() && !clientsWithActionTraffic.contains(playerUuid)) {
            getLogger().warning("No JMWS action payload has been received from " + player.getName() + " yet. If their waypoints do not sync, confirm their existing JMWS client mod loaded and try the client /jmws sync command.");
        }
    }

    int reloadPaperConfig() {
        PaperServerOptions.reload();

        int refreshedClients = 0;
        for (Player player : getServer().getOnlinePlayers()) {
            PlayerNetworkingHelper.sendHandshakeAndValidate(player.getUniqueId());
            refreshedClients++;
        }
        return refreshedClients;
    }
}
