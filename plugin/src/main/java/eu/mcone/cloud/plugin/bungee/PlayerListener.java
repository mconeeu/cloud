/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bungee;

import eu.mcone.cloud.core.network.packet.ServerPlayerCountUpdatePacket;
import eu.mcone.cloud.plugin.CloudPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

    private CloudPlugin instance;

    PlayerListener(CloudPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void on(PostLoginEvent e) {
        instance.getChannel().writeAndFlush(new ServerPlayerCountUpdatePacket(ProxyServer.getInstance().getOnlineCount()));
    }

    @EventHandler
    public void on(PlayerDisconnectEvent e) {
        instance.getChannel().writeAndFlush(new ServerPlayerCountUpdatePacket(ProxyServer.getInstance().getOnlineCount()));
    }

}
