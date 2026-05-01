package me.brynview.navidrohim.jmws.server;

import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.syncing.rules.ShareRuleManager;

public class JMWSServerCommon
{
    public static ServerSyncRegistry REGISTRY = new ServerSyncRegistry();
    public static ShareRuleManager SHARE_RULES = new ShareRuleManager();
}
