package me.brynview.navidrohim.jmws.client.screens;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;

import java.util.concurrent.atomic.AtomicInteger;

import static me.brynview.navidrohim.jmws.common.CommonClass.minecraftClientInstance;

public class ShareScreen extends Screen {

    private final Screen parent;
    private final ServerStatus.Players players;
    private final ClientObject<ClientWaypointWrapper> waypoint;

    public ShareScreen(Screen parent, ServerStatus.Players players, ClientObject<ClientWaypointWrapper> waypoint, Component title) {
        super(title);
        this.parent = parent;
        this.players = players;
        this.waypoint = waypoint;
    }


    @Override
    protected void init()
    {
        // Button that is used to quit game. Placed in the middle of the screen, lower half vertically if I am remembering correctly.
        this.addRenderableWidget(Button.builder(Component.literal("Close"), (button) -> this.minecraft.setScreen(parent)).bounds((this.width / 10) - 30, 15, 60, 20).build());
        addOnlinePlayers();
    }

    private void addOnlinePlayers()
    {
        AtomicInteger iter = new AtomicInteger();
        Constants.getLogger().info("Adding online player list " + this.players.sample());
        this.players.sample().forEach(player -> {
            Constants.getLogger().info("PTEST"+player.name());
            iter.addAndGet(1);
            this.addRenderableWidget(Button.builder(Component.literal(player.name()), (bnt) -> this.waypoint.shareWith(player.id())).bounds(20, 70 * iter.get(), this.width - 40, 50).build());
        });
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }

    public static void openShare(ClientObject<ClientWaypointWrapper> waypoint)
    {
        minecraftClientInstance.setScreen(new ShareScreen(minecraftClientInstance.screen, minecraftClientInstance.getCurrentServer().players, waypoint, Component.empty()));
    }
}
