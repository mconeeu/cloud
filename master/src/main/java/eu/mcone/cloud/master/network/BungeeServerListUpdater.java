/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.packet.ServerListUpdatePacketPlugin;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.CloudServer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BungeeServerListUpdater {

    private final static Set<ServerState> RELEVANT_STATES = new HashSet<>(Arrays.asList(ServerState.WAITING, ServerState.INGAME, ServerState.FULL));

    public static void unregisterServerOnAllBungees(CloudServer s) {
        for (Server server : MasterServer.getServer().getServers()) {
            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && RELEVANT_STATES.contains(server.getState())) {
                server.send(new ServerListUpdatePacketPlugin(s.getInfo(), ServerListUpdatePacketPlugin.Scope.REMOVE));
            }
        }
    }

    public static void registerServerOnAllBungees(CloudServer s) {
        if (RELEVANT_STATES.contains(s.getState())) {
            for (Server server : MasterServer.getServer().getServers()) {
                if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && RELEVANT_STATES.contains(server.getState())) {
                    server.send(new ServerListUpdatePacketPlugin(s.getInfo(), ServerListUpdatePacketPlugin.Scope.ADD));
                }
            }
        }
    }

    public static void registerAllServersOnBungee(CloudServer s) {
        for (Server server : MasterServer.getServer().getServers()) {
            if (!server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && RELEVANT_STATES.contains(server.getState())) {
                s.send(new ServerListUpdatePacketPlugin(server.getInfo(), ServerListUpdatePacketPlugin.Scope.ADD));
            }
        }
    }

}
