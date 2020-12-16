/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api.server;

import eu.mcone.cloud.core.api.template.Template;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;

import java.util.Map;
import java.util.UUID;

public interface SimpleServer {

    ServerInfo getInfo();

    Template getTemplate();

    Map<UUID, String> getPlayers();

    int getPlayerCount();

    ServerState getState();

}
