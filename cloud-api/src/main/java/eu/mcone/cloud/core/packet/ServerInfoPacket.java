/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import group.onegaming.networkmanager.api.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServerInfoPacket extends Packet {

    private ServerInfo serverInfo;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        writeServerInfo(out, serverInfo);
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        serverInfo = readServerInfo(in);
    }

    static void writeServerInfo(DataOutputStream out, ServerInfo info) throws IOException {
        out.writeUTF(info.getUuid().toString());
        out.writeUTF(info.getName());
        out.writeUTF(info.getTemplateName());
        out.writeInt(info.getTemplateID());
        out.writeLong(info.getRam());
        out.writeInt(info.getPort());
        out.writeInt(info.getMaxPlayers());
        out.writeBoolean(info.isStaticServer());
        out.writeUTF(info.getVersion().toString());
        out.writeUTF(info.getProperties());
    }

    static ServerInfo readServerInfo(DataInputStream in) throws IOException {
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

        ServerInfo info = new ServerInfo(uuid, name, templateName, maxplayers, templateId, ram, staticServer, version, properties);
        info.setPort(port);
        return info;
    }

}
