package me.navidrohim.jmws.client.command;

import info.journeymap.shaded.org.jetbrains.annotations.Nullable;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import me.navidrohim.jmws.common.helper.CommonHelper;
import me.navidrohim.jmws.common.helper.PlayerHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ClientCommandBase extends CommandBase {
    @Override public String getName() { return "jmws"; }

    @Override public String getUsage(ICommandSender sender) { return "/jmws <clearAll|sync|getSyncInterval|nextSync>"; }

    @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 0)
        {
            PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.commandUsage", getUsage(sender)), true, false, JMWSMessageType.WARNING);
            return;
        }

        if (CommonClass.getEnabledStatus())
        {
            switch (args[0])
            {
                case "sync": {
                    ClientCommands.sync();
                    break;
                }
                case "clearAll": {
                    ClientCommands.clearAllWaypoints();
                    break;
                }
                case "getSyncInterval": {
                    ClientCommands.getSyncInterval();
                    break;
                }
                case "nextSync": {
                    ClientCommands.nextSync();
                    break;
                }
                default: {
                    PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.unknownSubcommand"), true, false, JMWSMessageType.FAILURE);
                }
            }
        } else {
            PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.commandUnavailable"), true, false, JMWSMessageType.WARNING);
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "clearAll", "sync", "getSyncInterval", "nextSync");
        }
        return java.util.Collections.emptyList();
    }
}
