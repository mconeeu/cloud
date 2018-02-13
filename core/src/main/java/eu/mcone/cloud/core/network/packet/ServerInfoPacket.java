/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
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
            out.writeUTF(serverInfo.getState().toString());
            out.writeUTF(serverInfo.getTemplateName());
            out.writeInt(serverInfo.getTemplateID());
            out.writeInt(serverInfo.getRam());
            out.writeInt(serverInfo.getPort());
            out.writeInt(serverInfo.getMaxPlayers());
            out.writeUTF(serverInfo.getVersion().toString());

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
            ServerState state = ServerState.valueOf(input.readUTF());
            String templateName = input.readUTF();
            int templateId = input.readInt();
            int ram = input.readInt();
            int port = input.readInt();
            int maxplayers = input.readInt();
            ServerVersion version = ServerVersion.valueOf(input.readUTF());

            serverInfo = new ServerInfo(uuid, name, templateName, maxplayers, templateId, ram, version);
            serverInfo.setState(state);
            serverInfo.setPort(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
