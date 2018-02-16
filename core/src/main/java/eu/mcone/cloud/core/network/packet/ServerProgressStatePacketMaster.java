/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;

public class ServerProgressStatePacketMaster extends Packet {

    @Getter
    private Progress progress;

    @Getter
    private String event_class;

    public enum Progress{
        NOTPROGRESSING,
        INPROGRESSING
    }

    public ServerProgressStatePacketMaster(){}

    public ServerProgressStatePacketMaster(String event_class, Progress progress) {
        this.progress = progress;
        this.event_class = event_class;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(event_class);
            out.writeUTF(progress.toString());

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
            event_class = input.readUTF();
            progress = Progress.valueOf(input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}