package me.brynview.navidrohim.jmws.server.syncing.rules;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.server.JMWSServerCommon;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.syncing.rules.api.ShareRule;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ShareRuleManager extends HashMap<String, ShareRule>
{
    public static ShareWhileOnlineRule shareWhileOnline;

    public ShareRuleManager()
    {
        super();

        ShareRuleManager.shareWhileOnline = this.register(new ShareWhileOnlineRule());

    }

    public <E extends ShareRule> E register(E ins) {
        String id = ins.getRegistryKey();
        if (containsKey(id))
        {
            throw new IllegalArgumentException("Share rule with id '" + id + "' already exists.");
        }

        put(id, ins);
        return ins;
    }

    public static @Nullable ShareRule canShareTo(ServerObject object, ServerPlayer player)
    {
        for (ShareRule rule : object.rules) {
            boolean didPass = rule.passed(object, player);
            Constants.LoggerHolder.debug(didPass, "Rule " + rule.getRegistryKey());
            if (!didPass) {
                return rule;
            }
        }
        return null;
    }

    public static ImmutableList<ShareRule> getRuleset(ServerObject serverObject)
    {
        JsonElement rulesetData = serverObject.getRulesetData();
        List<ShareRule> rules = new ArrayList<>();

        // iterate through all rules and return list of instantiated rules from registry
        if (rulesetData != null)
        {
            for (JsonElement rule : rulesetData.getAsJsonArray()) {
                ShareRule ruleInstance = JMWSServerCommon.SHARE_RULES.get(rule.getAsString());
                if (ruleInstance == null) {
                    continue;
                }
                rules.add(ruleInstance);
            }
        }

        return ImmutableList.copyOf(rules);
    }
}
