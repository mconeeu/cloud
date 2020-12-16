/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import group.onegaming.networkmanager.api.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServerRegisterPacketPlugin extends Packet {

    private UUID serverUuid, wrapperUuid;
    private String hostname;
    private int port;
    private ServerState state;
    private ServerVersion version;
    private boolean staticServer;
    private Map<UUID, String> players;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        out.writeUTF(serverUuid.toString());
        out.writeUTF(wrapperUuid.toString());
        out.writeUTF(hostname);
        out.writeInt(port);
        out.writeUTF(state.toString());
        out.writeUTF(version.toString());
        out.writeBoolean(staticServer);

        out.writeInt(players.size());
        for (Map.Entry<UUID, String> e : players.entrySet()) {
            out.writeUTF(e.getKey().toString());
            out.writeUTF(e.getValue());
        }
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        serverUuid = UUID.fromString(in.readUTF());
        wrapperUuid = UUID.fromString(in.readUTF());
        hostname = in.readUTF();
        port = in.readInt();
        state = ServerState.valueOf(in.readUTF());
        version = ServerVersion.valueOf(in.readUTF());
        staticServer = in.readBoolean();

        players = new HashMap<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            players.put(
                    UUID.fromString(in.readUTF()),
                    in.readUTF()
            );
        }
    }

}
