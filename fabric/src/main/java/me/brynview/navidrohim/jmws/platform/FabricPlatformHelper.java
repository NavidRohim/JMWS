package me.brynview.navidrohim.jmws.platform;

import com.mojang.authlib.GameProfile;
import commonnetwork.api.Network;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.common.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.Optional;
import java.util.UUID;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String side() {return FabricLoader.getInstance().getEnvironmentType().toString();}

    @Override
    public void sendActionPayloadToClient(JMWSActionPayload payload, UUID playerUuid) {
        ServerPlayer player = CommonClass.minecraftServerInstance.getPlayerList().getPlayer(playerUuid);
        if (player != null) {
            Network.getNetworkHandler().sendToClient(payload, player, true);
        }
    }

    @Override
    public void sendHandshakePayloadToClient(JMWSHandshakePayload payload, UUID playerUuid) {
        ServerPlayer player = CommonClass.minecraftServerInstance.getPlayerList().getPlayer(playerUuid);
        if (player != null) {
            Network.getNetworkHandler().sendToClient(payload, player, true);
        }
    }

    @Override
    public boolean isOperator(UUID playerUuid) {
        Optional<GameProfile> profile = CommonClass.minecraftServerInstance.services().profileResolver().fetchById(playerUuid);
        return profile.isPresent() && CommonClass.minecraftServerInstance.getPlayerList().isOp(new NameAndId(profile.get()));
    }

}
