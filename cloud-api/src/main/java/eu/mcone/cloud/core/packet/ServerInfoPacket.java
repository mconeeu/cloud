/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import eu.mcone.networkmanager.api.network.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServerInfoPacket extends Packet {

    private static List<PacketHandler> handlerList = new ArrayList<>();
    @Override
    public List<PacketHandler> getHandlerList() {
        return handlerList;
    }

    private ServerInfo serverInfo;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
            out.writeUTF(serverInfo.getUuid().toString());
            out.writeUTF(serverInfo.getName());
            out.writeUTF(serverInfo.getTemplateName());
            out.writeInt(serverInfo.getTemplateID());
            out.writeLong(serverInfo.getRam());
            out.writeInt(serverInfo.getPort());
            out.writeInt(serverInfo.getMaxPlayers());
            out.writeBoolean(serverInfo.isStaticServer());
            out.writeUTF(serverInfo.getVersion().toString());
            out.writeUTF(serverInfo.getProperties());
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
            UUID uuid = UUID.fromString(in.readUTF());
            String name = in.readUTF();
            String templateName = in.readUTF();
            int templateId = in.readInt();
            long ram = in.readLong();
            int port = in.readInt();
            int maxplayers = in.readInt();
            boolean staticServer = in.readBoolean();
            ServerVersion version = ServerVersion.valueOf(in.readUTF());
            String properties = in.readUTF();

            serverInfo = new ServerInfo(uuid, name, templateName, maxplayers, templateId, ram, staticServer, version, properties);
            serverInfo.setPort(port);
    }
}
