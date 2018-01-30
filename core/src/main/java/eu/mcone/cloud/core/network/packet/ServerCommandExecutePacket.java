/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class ServerCommandExecutePacket extends Packet{

    @Getter
    private UUID serverUuid;
    @Getter
    private String cmd;

    public ServerCommandExecutePacket() {}

    public ServerCommandExecutePacket(UUID serverUuid, String cmd) {
        this.serverUuid = serverUuid;
        this.cmd = cmd;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(serverUuid.toString());
            out.writeUTF(cmd);

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
            serverUuid = UUID.fromString(input.readUTF());
            cmd = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
