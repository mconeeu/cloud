/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bungee;

import eu.mcone.cloud.core.packet.ServerPlayerCountUpdatePacketPlugin;
import eu.mcone.cloud.core.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.plugin.CloudPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

    private CloudPlugin instance;

    PlayerListener(CloudPlugin instance) {
        this.instance = instance;
    }

    public void on(PreLoginEvent e) {
        if (instance.getNettyClient().getPacketManager().getChannel() == null) {
            e.setCancelled(true);
            e.setCancelReason(
                    new TextComponent(TextComponent.fromLegacyText("§f§lMC ONE §3Minecraftnetzwerk"
                            + "\n§4§oDer Netzwerkserver ist noch nicht verbunden!"
                            + "\n§r"
                            + "\n§fBitte versuche es in wenigen Sekunden nochmal..."
                            + "\n§7Ohne die Verbindung zum Netzwerkserver können wir"
                            + "\n."))
            );
        }
    }

    @EventHandler
    public void on(PostLoginEvent e) {
        instance.getNettyClient().getPacketManager().send(
                new ServerPlayerCountUpdatePacketPlugin(
                        instance.getServerUuid(),
                        e.getPlayer().getUniqueId(),
                        e.getPlayer().getName(),
                        ServerPlayerCountUpdatePacketPlugin.Method.ADD
                )
        );

        if (instance.getState().equals(ServerState.WAITING)) {
            if (ProxyServer.getInstance().getOnlineCount() >= ProxyServer.getInstance().getConfig().getListeners().iterator().next().getMaxPlayers()) {
                instance.getNettyClient().getPacketManager().send(new ServerUpdateStatePacket(instance.getServerUuid(), ServerState.FULL));
                instance.setState(ServerState.FULL);
            }
        }
    }

    @EventHandler
    public void on(PlayerDisconnectEvent e) {
        int onlinePlayers = ProxyServer.getInstance().getOnlineCount()-1;
        instance.getNettyClient().getPacketManager().send(
                new ServerPlayerCountUpdatePacketPlugin(
                        instance.getServerUuid(),
                        e.getPlayer().getUniqueId(),
                        e.getPlayer().getName(),
                        ServerPlayerCountUpdatePacketPlugin.Method.REMOVE
                )
        );

        if (instance.getState().equals(ServerState.FULL)) {
            if (onlinePlayers < ProxyServer.getInstance().getConfig().getListeners().iterator().next().getMaxPlayers()) {
                instance.getNettyClient().getPacketManager().send(new ServerUpdateStatePacket(instance.getServerUuid(), ServerState.WAITING));
                instance.setState(ServerState.WAITING);
            }
        }
    }

}
