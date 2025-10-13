package me.navidrohim.jmws.client.config;

import me.navidrohim.jmws.JMWS;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.*;

import java.util.List;

public class NonEditableConfig extends GuiConfig {
    public NonEditableConfig(GuiScreen parentScreen) {
        super(parentScreen, Constants.MODID, "Test");

        IConfigElement elem = (IConfigElement) new ImmutableButton(this, null, new DummyConfigElement("svrDisplay", false, ConfigGuiType.BOOLEAN, "test"));
        this.entryList.listEntries
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        super.actionPerformed(guiButton);
        Constants.getLogger().info("conf");
    }
}
