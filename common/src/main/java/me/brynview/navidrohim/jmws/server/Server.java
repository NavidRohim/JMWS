package me.brynview.navidrohim.jmws.server;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;

public class Server {
    public static final SuggestionProvider<CommandSourceStack> WAYPOINT_SUGGESTER =
            (context, builder) -> {
                List<String> names = JMWSServerIO.getObjectsForUser(context.getSource().getPlayer().getUUID(), FetchType.WAYPOINT)
                        .stream()
                        .map(ServerObject::getName)
                        .toList();

                return SharedSuggestionProvider.suggest(names, builder);
            };
}
