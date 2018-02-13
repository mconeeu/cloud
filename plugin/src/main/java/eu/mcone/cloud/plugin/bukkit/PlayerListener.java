/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import eu.mcone.cloud.core.network.packet.ServerPlayerCountUpdatePacketPlugin;
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
        instance.getChannel().writeAndFlush(new ServerPlayerCountUpdatePacketPlugin(Bukkit.getOnlinePlayers().size()));
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        instance.getChannel().writeAndFlush(new ServerPlayerCountUpdatePacketPlugin(Bukkit.getOnlinePlayers().size()));
    }

}
