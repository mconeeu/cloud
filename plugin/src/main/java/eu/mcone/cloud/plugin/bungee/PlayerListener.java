/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bungee;

import eu.mcone.cloud.core.network.packet.ServerPlayerCountUpdatePacketPlugin;
import eu.mcone.cloud.core.network.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.plugin.CloudPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bukkit.Bukkit;

public class PlayerListener implements Listener {

    private CloudPlugin instance;

    PlayerListener(CloudPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void on(PostLoginEvent e) {
        instance.send(new ServerPlayerCountUpdatePacketPlugin(instance.getServerUuid(), ProxyServer.getInstance().getOnlineCount()));

        if (instance.getState().equals(ServerState.WAITING)) {
            if (ProxyServer.getInstance().getOnlineCount() >= ProxyServer.getInstance().getConfig().getPlayerLimit()) {
                instance.send(new ServerUpdateStatePacket(instance.getServerUuid(), ServerState.FULL));
            }
        }
    }

    @EventHandler
    public void on(PlayerDisconnectEvent e) {
        instance.send(new ServerPlayerCountUpdatePacketPlugin(instance.getServerUuid(), ProxyServer.getInstance().getOnlineCount()));

        if (instance.getState().equals(ServerState.FULL)) {
            if (ProxyServer.getInstance().getOnlineCount() < ProxyServer.getInstance().getConfig().getPlayerLimit()) {
                instance.send(new ServerUpdateStatePacket(instance.getServerUuid(), ServerState.WAITING));
            }
        }
    }

}
