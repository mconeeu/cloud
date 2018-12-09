/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.handler;

import eu.mcone.cloud.core.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Bukkit;
import eu.mcone.cloud.wrapper.server.BungeeCord;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class ServerInfoHandler implements PacketHandler<ServerInfoPacket> {

    @Override
    public void onPacketReceive(ServerInfoPacket packet, ChannelHandlerContext chc) {
        ServerInfo info = packet.getServerInfo();
        log.fine("new ServerInfoPacket (UUID: "+packet.getServerInfo().getUuid()+", NAME: "+packet.getServerInfo().getName()+")");

        for (Server s : WrapperServer.getInstance().getServers()) {
            if (s.getInfo().getUuid().equals(info.getUuid())) {
                s.setInfo(info);
                return;
            }
        }

        switch (packet.getServerInfo().getVersion()) {
            case BUNGEE: new BungeeCord(packet.getServerInfo()); break;
            case SPIGOT: new Bukkit(packet.getServerInfo()); break;
        }
    }

}
