/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.listener;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.packet.CloudServerForwardedRequestPacket;
import eu.mcone.cloud.core.packet.CloudServerForwardedResponsePacket;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.CloudServer;
import group.onegaming.networkmanager.api.messaging.request.ClientMessageRequestListener;
import group.onegaming.networkmanager.api.packet.ClientMessageRequestPacket;
import group.onegaming.networkmanager.api.packet.ClientMessageResponsePacket;
import group.onegaming.networkmanager.host.api.ModuleHost;
import lombok.extern.java.Log;

@Log
public class CloudServerForwardRequestListener implements ClientMessageRequestListener {

    @Override
    public ClientMessageResponsePacket onClientRequest(ClientMessageRequestPacket packet) {
        if (packet instanceof CloudServerForwardedRequestPacket) {
            CloudServerForwardedRequestPacket request = (CloudServerForwardedRequestPacket) packet;
            Server to = MasterServer.getServer().getServer(request.getToServer());

            if (to != null) {
                ModuleHost.getInstance().getPacketManager().sendClientRequest(((CloudServer) to).getChannel(), packet, packet2 -> {
                    if (packet2 instanceof CloudServerForwardedResponsePacket) {
                        CloudServerForwardedResponsePacket response = (CloudServerForwardedResponsePacket) packet2;
                        Server from = MasterServer.getServer().getServer(response.getFromServer());

                        if (from != null) {
                            from.send(packet2);
                        }
                    } else {
                        log.severe("Response of CloudServerForwardedRequest could not be casted to CloudServerForwardedResponse!");
                    }
                });
            }
        }

        return null;
    }

}
