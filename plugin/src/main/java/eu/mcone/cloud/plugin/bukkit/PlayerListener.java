/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import eu.mcone.cloud.core.network.packet.ServerPlayerCountUpdatePacketPlugin;
import eu.mcone.cloud.core.network.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.plugin.CloudPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private CloudPlugin instance;

    PlayerListener(CloudPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        instance.send(new ServerPlayerCountUpdatePacketPlugin(instance.getServerUuid(), Bukkit.getOnlinePlayers().size()));

        if (instance.getState().equals(ServerState.WAITING)) {
            if (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) {
                instance.send(new ServerUpdateStatePacket(instance.getServerUuid(), ServerState.FULL));
            }
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        int onlinePlayer = Bukkit.getOnlinePlayers().size()-1;
        instance.send(new ServerPlayerCountUpdatePacketPlugin(instance.getServerUuid(), onlinePlayer));

        if (instance.getState().equals(ServerState.FULL)) {
            if (onlinePlayer < Bukkit.getMaxPlayers()) {
                instance.send(new ServerUpdateStatePacket(instance.getServerUuid(), ServerState.WAITING));
            }
        }
    }

}
