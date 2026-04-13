package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.client.ui.scroll.ObjectSharePanel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static me.brynview.navidrohim.jmws.common.CommonClass.minecraftClientInstance;

public class ShareScreen <T extends ClientObjectWrapper<?>> extends Screen {

    private final Screen parent;
    private final ClientWaypointWrapper waypoint;

    private @Nullable LinearLayout layout;
    private ObjectSharePanel<ClientWaypointWrapper> sharePanel;

    public ShareScreen(Screen parent, ClientWaypointWrapper waypoint, Component title) {
        super(title);
        this.parent = parent;
        this.waypoint = waypoint;
    }

    @Override
    protected void init()
    {
        // Define the sharing panel and add all shared objects on this client to panel
        this.sharePanel = new ObjectSharePanel<>(minecraftClientInstance,  width / 4 * 3, this.height, 0, 64, waypoint);
        this.sharePanel.addSelf(0);
        this.sharePanel.addWidgets();

        // Add layout


        // Add close button and share panel
        this.addRenderableWidget(this.sharePanel);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (bnt) -> minecraftClientInstance.setScreen(parent)).bounds(this.width - 50, this.height - 50, 50, 50).build());

    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    public static void openShare(ClientWaypointWrapper waypoint)
    {
        minecraftClientInstance.setScreen(new ShareScreen<ClientWaypointWrapper>(minecraftClientInstance.screen, waypoint, Component.empty()));
    }
}
