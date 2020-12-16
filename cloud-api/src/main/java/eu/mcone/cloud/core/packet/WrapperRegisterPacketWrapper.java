/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

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
public class WrapperRegisterPacketWrapper extends Packet {

    private long ram;
    private UUID uuid;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        out.writeLong(ram);
        out.writeUTF(uuid.toString());
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        ram = in.readLong();
        uuid = UUID.fromString(in.readUTF());
    }

}
