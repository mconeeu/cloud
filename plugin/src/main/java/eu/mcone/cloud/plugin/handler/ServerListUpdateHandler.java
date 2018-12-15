/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.handler;

import eu.mcone.cloud.core.packet.ServerListUpdatePacketPlugin;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;

import java.net.InetSocketAddress;

public class ServerListUpdateHandler implements PacketHandler<ServerListUpdatePacketPlugin> {

    @Setter
    private static boolean newConnection = false;

    @Override
    public void onPacketReceive(ServerListUpdatePacketPlugin packet, ChannelHandlerContext chc) {
        if (newConnection) {
            ProxyServer.getInstance().getServers().clear();
            newConnection = false;
        }

        if (packet.getScope().equals(ServerListUpdatePacketPlugin.Scope.ADD)) {
            System.out.println("adding server " + packet.getName() + " to bc server map");
            ProxyServer.getInstance().getServers().put(
                    packet.getName(),
                    ProxyServer.getInstance().constructServerInfo(
                            packet.getName(),
                            InetSocketAddress.createUnresolved(packet.getHostname(), packet.getPort()),
                            "§f§lMC ONE §3Server §8» §7" + packet.getName(),
                            false
                    )
            );
        } else if (packet.getScope().equals(ServerListUpdatePacketPlugin.Scope.REMOVE)) {
            ProxyServer.getInstance().getServers().remove(packet.getName());
        }
    }

}
