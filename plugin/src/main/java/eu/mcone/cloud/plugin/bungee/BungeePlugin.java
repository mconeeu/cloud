/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bungee;

import eu.mcone.cloud.api.plugin.bungee.BungeeCloudPlugin;
import eu.mcone.cloud.plugin.CloudPlugin;
import eu.mcone.cloud.plugin.bungee.server.ReconnectHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BungeePlugin extends Plugin implements BungeeCloudPlugin {

    private CloudPlugin instance;

    @Override
    public void onLoad() {
        instance = new CloudPlugin(this);
    }

    @Override
    public void onEnable() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerListener(this.instance));
        ProxyServer.getInstance().getServers().clear();
        ProxyServer.getInstance().setReconnectHandler(new ReconnectHandler());
    }

    @Override
    public void onDisable() {
        instance.unload();
    }

    @Override
    public void runAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(this, runnable);
    }

    @Override
    public Map<UUID, String> getPlayers() {
        Map<UUID, String> result = new HashMap<>();
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            result.put(p.getUniqueId(), p.getName());
        }

        return result;
    }

    @Override
    public ServerInfo getFallbackServer() {
        return ((ReconnectHandler) ProxyServer.getInstance().getReconnectHandler()).getFallbackServer();
    }

}
