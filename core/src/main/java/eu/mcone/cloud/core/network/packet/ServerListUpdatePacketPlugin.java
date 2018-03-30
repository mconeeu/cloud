/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class ServerListUpdatePacketPlugin extends Packet {

    public enum Scope {
        ADD, REMOVE
    }

    @Getter
    private Scope scope;
    @Getter
    private String name, hostname;
    @Getter
    private int port;

    public ServerListUpdatePacketPlugin() {}

    public ServerListUpdatePacketPlugin(ServerInfo info, Scope scope) {
        this.scope = scope;
        this.name = info.getName();

        if (scope.equals(Scope.ADD)) {
            this.hostname = info.getHostname();
            this.port = info.getPort();
        }
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(scope.toString());
            out.writeUTF(name);

            if (scope.equals(Scope.ADD)) {
                out.writeUTF(hostname);
                out.writeInt(port);
            }

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
            scope = Scope.valueOf(input.readUTF());
            name = input.readUTF();

            if (scope.equals(Scope.ADD)) {
                hostname = input.readUTF();
                port = input.readInt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AllArgsConstructor
    public class MinServerInfo {
        @Getter
        private UUID uuid;
        @Getter
        private String name, hostname;
        @Getter
        private int port;
    }

}
