/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bungee.server;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReconnectHandler implements net.md_5.bungee.api.ReconnectHandler {

    @Override
    public ServerInfo getServer(ProxiedPlayer p) {
        ServerInfo server = getFallbackServer();

        if (server == null) {
            p.disconnect(new TextComponent(TextComponent.fromLegacyText("§4§oEs konnte kein Server gefunden werden!")));
            return null;
        } else {
            return server;
        }
    }

    @Override
    public void setServer(ProxiedPlayer p) {}

    @Override
    public void save() {}

    @Override
    public void close() {}

    public ServerInfo getFallbackServer() {
        Map<ServerInfo, Integer> servers = new HashMap<>();

        for (ServerInfo s : ProxyServer.getInstance().getServers().values()) {
            if (s.getName().contains("Lobby-")) {
                servers.put(s, s.getPlayers().size());
            }
        }

        if (servers.size() > 0) {
            return Collections.min(servers.entrySet(), HashMap.Entry.comparingByValue()).getKey();
        } else {
            return null;
        }
    }

}
