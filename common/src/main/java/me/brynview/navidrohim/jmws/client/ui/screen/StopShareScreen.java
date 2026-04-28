package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.elements.Checkbox;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.HasScrollableList;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.client.ui.list.PlayerSelectionList;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2i;

public class StopShareScreen extends NotificationAlertScreen implements HasScrollableList
{
    private Checkbox checkbox;
    private PlayerSelectionList playerSelectionList;

    private final ClientObjectWrapper<?> object;

    public StopShareScreen(Screen parent, ClientObjectWrapper<?> object)
    {
        super(parent, true);
        this.object = object;
    }

    @Override
    protected void init()
    {
        super.init();

        LinearLayout rightButtonColumn = LinearLayout.vertical();
        LinearLayout horizontalElementRow = LinearLayout.horizontal().spacing(4);

        this.playerSelectionList = new PlayerSelectionList(JMWSCommon.minecraftClientInstance, 50, this.object, this);
        RenderUtils.setDimensionsForList(this.playerSelectionList, this.width, this.height);

        this.checkbox = Checkbox.buildSelectAllCheckbox(button -> {
            if (button.isChecked) {
                this.playerSelectionList.selectAll();
            } else {
                this.playerSelectionList.unselectAll();
            }
        });

        //        rightButtonColumn.addChild()
        horizontalElementRow.addChild(rightButtonColumn);
        horizontalElementRow.addChild(this.playerSelectionList);
        horizontalElementRow.addChild(checkbox);

        horizontalElementRow.arrangeElements();
        FrameLayout.centerInRectangle(horizontalElementRow, 0, 0, this.width, this.height);
        horizontalElementRow.visitWidgets(this::addRenderableWidget);

        this.playerSelectionList.addWidgets();
    }

    @Override
    protected Vector2i getDrawLocationForAlert() {
        return RenderUtils.getPositionRelativeToList(this.playerSelectionList);
    }

    @Override
    public void entryPressed()
    {
        if (playerSelectionList.getSelectedEntries().isEmpty())
        {
            this.checkbox.isChecked = false;
            this.playerSelectionList.isSelectingAll = false;
        } else if (playerSelectionList.getSelectedEntries().size() == playerSelectionList.children().size())
        {
            this.checkbox.isChecked = true;
            this.playerSelectionList.isSelectingAll = true;
        }
    }

    public static void open(ClientObjectWrapper<?> clientObject)
    {
        JMWSClientCommon.setCurrentUIScreen(new StopShareScreen(JMWSCommon.minecraftClientInstance.screen, clientObject));
    }
}
