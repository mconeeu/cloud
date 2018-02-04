/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;

public class ServerResultPacketWrapper extends Packet {

    @Getter
    private String message;
    @Getter
    private Result result;
    @Getter
    private String resultClass;

    public enum Result {
        ERROR,
        COOMMAND_ERROR,
        SERVER_ERROR,
        COMMAND,
        INFORMATION,
        SUCCESSFUL,
    }

    public ServerResultPacketWrapper() {}

    public ServerResultPacketWrapper(String result_class, String message, Result result) {
        this.resultClass = result_class;
        this.message = message;
        this.result = result;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(resultClass.toString());
            out.writeUTF(message.toString());
            out.writeUTF(result.toString());

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

            resultClass = input.readUTF();
            message = input.readUTF();
            result = result.valueOf(input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
