/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.packet.ServerRegisterPacketPlugin;
import eu.mcone.cloud.core.server.PluginRegisterData;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class ServerRegisterHandler implements PacketHandler<ServerRegisterPacketPlugin> {

    @Override
    public void onPacketReceive(ServerRegisterPacketPlugin packet, ChannelHandlerContext chc) {
        log.fine("new ServerRegisterPacketPlugin (UUID: "+packet.getServerUuid()+", PORT: "+packet.getPort()+")");
        Server s = MasterServer.getInstance().getServer(packet.getServerUuid());

        if (s != null) {
            s.registerPluginData(new PluginRegisterData(chc.channel(), packet));
        } else {
            try {
                WrapperRegisterFromStandaloneHandler.registeringServers.put(packet.getServerUuid(), new PluginRegisterData(chc.channel(), packet));
                log.info("Server with uuid " + packet.getServerUuid() + " tried to register itself from " + packet.getHostname() + " but the server is not known! Put in waitlist.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
