package eu.mcone.cloud.master.listener;

import eu.mcone.cloud.core.packet.CloudInfoResponsePacket;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.networkmanager.api.messaging.request.ClientMessageRequestListener;
import eu.mcone.networkmanager.api.packet.ClientMessageRequestPacket;
import eu.mcone.networkmanager.api.packet.ClientMessageResponsePacket;

public class CloudInfoRequestListener implements ClientMessageRequestListener {

    @Override
    public ClientMessageResponsePacket onClientRequest(ClientMessageRequestPacket packet) {
        return new CloudInfoResponsePacket(packet.getRequestUuid(), MasterServer.getServer());
    }

}
