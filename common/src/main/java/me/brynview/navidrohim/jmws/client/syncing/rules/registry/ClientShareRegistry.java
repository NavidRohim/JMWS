package me.brynview.navidrohim.jmws.client.syncing.rules.registry;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.syncing.rules.ClientShareWhileOnlineRule;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ClientShareRegistry extends HashMap<String, ClientShareRule> {

    public static ClientShareWhileOnlineRule shareWhileOnline;


    public ClientShareRegistry() {
        super();

        ClientShareRegistry.shareWhileOnline = this.register(new ClientShareWhileOnlineRule());
    }

    public <E extends ClientShareRule> E register(E ins) {
        String id = ins.getRegistryKey();
        if (containsKey(id))
        {
            throw new IllegalArgumentException("Client share rule with id '" + id + "' already exists.");
        }

        put(id, ins);
        return ins;
    }
    public static ImmutableSet<ClientShareRule> getRulesFromJsonString(String json) {
        // `json` is just a list of strings formatted in JSON. Parse it and return a set of rules
        JsonArray rules = JsonParser.parseString(json).getAsJsonArray();
        Set<ClientShareRule> rulesSet = new HashSet<>();
        for (JsonElement rule : rules)
        {
            @Nullable ClientShareRule ruleObj = JMWSClientCommon.clientShareRegistry.get(rule.getAsString());
            if (ruleObj != null)
            {
                rulesSet.add(ruleObj);
            } else {
                Constants.getLogger().warn("Rule {} not found in registry.", rule.getAsString());
                Constants.getLogger().warn("Available rules: {}", JMWSClientCommon.clientShareRegistry.keySet());
            }
        }

        return ImmutableSet.copyOf(rulesSet);
    }

    public static String getRulesetFromRules(ClientShareRule... rules) {
        // Iterate through all rules, then format to json string and return

        if (rules.length == 0)
        {
            // Return an empty JSON array, cannot use new JsonArray().getAsString() as it throws IllegalStateException if empty, which it is.

        }

        JsonArray jsonRules = new JsonArray();
        for (ClientShareRule rule : rules) {
            jsonRules.add(rule.getRegistryKey());
        }
        return jsonRules.getAsString();
    }
}
