/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import eu.mcone.networkmanager.api.network.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WrapperRegisterFromStandalonePacketWrapper extends Packet {

    private static List<PacketHandler> handlerList = new ArrayList<>();
    @Override
    public List<PacketHandler> getHandlerList() {
        return handlerList;
    }

    private Long ram;
    private UUID uuid;
    private Map<UUID, String> servers;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
            out.writeLong(ram);
            out.writeUTF(uuid.toString());
            out.writeInt(servers.size());
            for (HashMap.Entry<UUID, String> e : servers.entrySet()) {
                out.writeUTF(e.getKey().toString());
                out.writeUTF(e.getValue());
            }
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
            ram = in.readLong();
            uuid = UUID.fromString(in.readUTF());

            int size = in.readInt();
            servers = new HashMap<>();

            for (int i = 0; i < size; i++) {
                UUID uuid = UUID.fromString(in.readUTF());
                String name = in.readUTF();

                servers.put(uuid, name);
            }
    }

}
