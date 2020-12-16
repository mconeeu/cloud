/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.listener;

import eu.mcone.cloud.core.packet.CloudInfoResponsePacket;
import eu.mcone.cloud.master.MasterServer;
import group.onegaming.networkmanager.api.messaging.request.ClientMessageRequestListener;
import group.onegaming.networkmanager.api.packet.ClientMessageRequestPacket;
import group.onegaming.networkmanager.api.packet.ClientMessageResponsePacket;

public class CloudInfoRequestListener implements ClientMessageRequestListener {

    @Override
    public ClientMessageResponsePacket onClientRequest(ClientMessageRequestPacket packet) {
        return new CloudInfoResponsePacket(packet.getRequestUuid(), MasterServer.getServer());
    }

}
