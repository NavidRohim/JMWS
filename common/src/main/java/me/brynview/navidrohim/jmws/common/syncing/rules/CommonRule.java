package me.brynview.navidrohim.jmws.common.syncing.rules;

import net.minecraft.network.chat.Component;

import java.awt.*;

public interface CommonRule
{
    Component getFailureMessage();
    String getRegistryKey();
}
