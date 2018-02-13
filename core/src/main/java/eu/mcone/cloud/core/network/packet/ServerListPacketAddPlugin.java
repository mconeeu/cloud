/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerListPacketAddPlugin extends Packet {

    @Getter
    private String name;
    @Getter
    private String hostname;
    @Getter
    private int port;

    public ServerListPacketAddPlugin() {}

    public ServerListPacketAddPlugin(ServerInfo info) {
        this.name = info.getName();
        this.hostname = info.getHostname();
        this.port = info.getPort();
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(name);
            out.writeUTF(hostname);
            out.writeInt(port);

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
            name = input.readUTF();
            hostname = input.readUTF();
            port = input.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
