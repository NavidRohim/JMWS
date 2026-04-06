package me.brynview.navidrohim.jmws.client.screens;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;

import java.util.concurrent.atomic.AtomicInteger;

import static me.brynview.navidrohim.jmws.common.CommonClass.minecraftClientInstance;

public class ShareScreen extends Screen {

    private final Screen parent;
    private final ServerStatus.Players players;
    private final ClientWaypointWrapper waypoint;

    private final static int BUTTON_WIDTH = 100;
    private final static int BUTTON_HEIGHT = 20;

    private final int BUTTON_CLOSE_X;

    public ShareScreen(Screen parent, ServerStatus.Players players, ClientWaypointWrapper waypoint, Component title) {
        super(title);
        this.parent = parent;
        this.players = players;
        this.waypoint = waypoint;

        this.BUTTON_CLOSE_X = (this.width / 10) - 30;

    }


    @Override
    protected void init()
    {
        // Button that is used to quit game. Placed in the middle of the screen, lower half vertically if I am remembering correctly.
        this.addRenderableWidget(Button.builder(Component.literal("Close"), (button) -> this.minecraft.setScreen(parent)).bounds(BUTTON_CLOSE_X, 15, 60, 20).build());
        addOnlinePlayers();
    }

    private void addOnlinePlayers()
    {
        AtomicInteger iter = new AtomicInteger();
        Constants.getLogger().info("Adding online player list " + this.players.sample());
        this.players.sample().forEach(player -> {
            Constants.getLogger().info("PTEST"+player.name());
            iter.addAndGet(1);
            this.addRenderableWidget(Button.builder(Component.literal(player.name()), (bnt) -> this.waypoint.addSharedTo(player.id())).bounds(20, BUTTON_CLOSE_X + 5 + BUTTON_HEIGHT * iter.get(), this.width - 40, BUTTON_HEIGHT).build());
        });
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }

    public static void openShare(ClientWaypointWrapper waypoint)
    {
        minecraftClientInstance.setScreen(new ShareScreen(minecraftClientInstance.screen, minecraftClientInstance.getCurrentServer().players, waypoint, Component.empty()));
    }
}
