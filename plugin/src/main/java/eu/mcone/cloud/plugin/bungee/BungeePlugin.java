/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bungee;

import eu.mcone.cloud.core.network.packet.ServerUpdateStatePacketPlugin;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.plugin.CloudPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePlugin extends Plugin {

    private CloudPlugin instance;

    @Override
    public void onLoad() {
        instance = new CloudPlugin();
    }

    @Override
    public void onEnable() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerListener(this.instance));
        ProxyServer.getInstance().getServers().clear();
        ProxyServer.getInstance().getScheduler().runAsync(this, () -> instance.send(new ServerUpdateStatePacketPlugin(instance.getServerUuid(), ServerState.WAITING)));
    }

    @Override
    public void onDisable() {
        instance.send(new ServerUpdateStatePacketPlugin(instance.getServerUuid(), ServerState.OFFLINE));
        instance.unload();
    }

}
