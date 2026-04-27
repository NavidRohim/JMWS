package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.list.PlayerSelectionList;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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

        LinearLayout rightButtonColumn = LinearLayout.vertical().spacing(4);
        LinearLayout horizontalElementRow = LinearLayout.vertical().spacing(6);

        this.playerSelectionList = new PlayerSelectionList(JMWSCommon.minecraftClientInstance, 50, this.object);
        RenderUtils.setDimensionsForList(this.playerSelectionList, this.width, this.height);

        Checkbox selectDeselectAllCheckbox = Checkbox.builder(Component.literal("s/d"), minecraft.font).build();

        horizontalElementRow.addChild(selectDeselectAllCheckbox);
        horizontalElementRow.addChild(this.playerSelectionList);
        horizontalElementRow.addChild(rightButtonColumn);

        horizontalElementRow.arrangeElements();
        FrameLayout.centerInRectangle(horizontalElementRow, 0, 0, this.width, this.height);
        horizontalElementRow.visitWidgets(this::addRenderableWidget);

        this.playerSelectionList.addWidgets();
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
