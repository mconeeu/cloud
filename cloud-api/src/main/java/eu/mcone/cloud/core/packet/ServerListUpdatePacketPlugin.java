/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import eu.mcone.networkmanager.api.network.packet.Packet;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ServerListUpdatePacketPlugin extends Packet {

    private static List<PacketHandler> handlerList = new ArrayList<>();
    @Override
    public List<PacketHandler> getHandlerList() {
        return handlerList;
    }

    public enum Scope {
        ADD, REMOVE
    }

    private Scope scope;
    private String name, hostname;
    private int port;

    public ServerListUpdatePacketPlugin(ServerInfo info, Scope scope) {
        this.scope = scope;
        this.name = info.getName();

        if (scope.equals(Scope.ADD)) {
            this.hostname = info.getHostname();
            this.port = info.getPort();
        }
    }

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
            out.writeUTF(scope.toString());
            out.writeUTF(name);

            if (scope.equals(Scope.ADD)) {
                out.writeUTF(hostname);
                out.writeInt(port);
            }
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
            scope = Scope.valueOf(in.readUTF());
            name = in.readUTF();

            if (scope.equals(Scope.ADD)) {
                hostname = in.readUTF();
                port = in.readInt();
            }
    }

}