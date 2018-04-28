/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bungee;

import eu.mcone.cloud.plugin.CloudPlugin;
import eu.mcone.cloud.plugin.bungee.server.ReconnectHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePlugin extends Plugin implements eu.mcone.cloud.api.plugin.Plugin {

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
    public int getPlayerCount() {
        return ProxyServer.getInstance().getOnlineCount();
    }
}
