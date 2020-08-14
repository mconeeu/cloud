/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import eu.mcone.cloud.core.packet.ServerPlayerCountUpdatePacketPlugin;
import eu.mcone.cloud.core.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.plugin.CloudPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private CloudPlugin instance;

    PlayerListener(CloudPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void on(PlayerLoginEvent e) {
        if (instance.getNettyClient().getPacketManager().getChannel() == null) {
            e.setKickMessage("Cloudsystem still not available");
            e.getPlayer().kickPlayer("Cloudsystem still not available");
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        instance.getNettyClient().getPacketManager().send(
                new ServerPlayerCountUpdatePacketPlugin(
                        instance.getServerUuid(),
                        e.getPlayer().getUniqueId(),
                        e.getPlayer().getName(),
                        ServerPlayerCountUpdatePacketPlugin.Method.ADD
                )
        );

        if (instance.getState().equals(ServerState.WAITING)) {
            if (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) {
                instance.getNettyClient().getPacketManager().send(new ServerUpdateStatePacket(instance.getServerUuid(), ServerState.FULL));
            }
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        int onlinePlayer = Bukkit.getOnlinePlayers().size() - 1;
        instance.getNettyClient().getPacketManager().send(
                new ServerPlayerCountUpdatePacketPlugin(
                        instance.getServerUuid(),
                        e.getPlayer().getUniqueId(),
                        e.getPlayer().getName(),
                        ServerPlayerCountUpdatePacketPlugin.Method.REMOVE
                )
        );

        if (instance.getState().equals(ServerState.FULL)) {
            if (onlinePlayer < Bukkit.getMaxPlayers()) {
                instance.getNettyClient().getPacketManager().send(new ServerUpdateStatePacket(instance.getServerUuid(), ServerState.WAITING));
            }
        }
    }

}
