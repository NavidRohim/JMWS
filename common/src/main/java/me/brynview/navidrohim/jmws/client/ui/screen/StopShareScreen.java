package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.share_panel.PlayerSelectionList;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2i;

public class StopShareScreen extends NotificationAlertScreen
{
    private PlayerSelectionList playerSelectionList;
    private final ClientObjectWrapper<?> object;

    public StopShareScreen(Screen parent, ClientObjectWrapper<?> object)
    {
        super(parent);
        this.object = object;
    }

    @Override
    protected void init()
    {
        this.playerSelectionList = new PlayerSelectionList(JMWSCommon.minecraftClientInstance, 0, 0, 0, 0, 50);
        RenderUtils.setDimensionsForList(this.playerSelectionList, this.width, this.height);

        this.addRenderableWidget(this.playerSelectionList);
    }

    @Override
    protected Vector2i getDrawLocationForAlert() {
        return RenderUtils.getPositionRelativeToList(this.playerSelectionList);
    }

    public static void open(ClientObjectWrapper<?> clientObject)
    {
        JMWSClientCommon.setCurrentUIScreen(new StopShareScreen(JMWSCommon.minecraftClientInstance.screen, clientObject));
    }
}
