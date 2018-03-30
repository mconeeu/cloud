/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class ServerInfoPacket extends Packet {

    @Getter
    private ServerInfo serverInfo;

    public ServerInfoPacket() {}

    public ServerInfoPacket(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
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

            byte[] result = stream.toByteArray();
            byteBuf.writeInt(result.length);
            byteBuf.writeBytes(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read(ByteBuf byteBuf) {
        byte[] msg = new byte[byteBuf.readInt()];
        byteBuf.readBytes(msg);

        DataInputStream input = new DataInputStream(new ByteArrayInputStream(msg));
        try {
            UUID uuid = UUID.fromString(input.readUTF());
            String name = input.readUTF();
            String templateName = input.readUTF();
            int templateId = input.readInt();
            long ram = input.readLong();
            int port = input.readInt();
            int maxplayers = input.readInt();
            boolean staticServer = input.readBoolean();
            ServerVersion version = ServerVersion.valueOf(input.readUTF());
            String properties = input.readUTF();

            serverInfo = new ServerInfo(uuid, name, templateName, maxplayers, templateId, ram, staticServer, version, properties);
            serverInfo.setPort(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
