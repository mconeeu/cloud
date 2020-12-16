/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api.template;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.server.ServerVersion;

import java.util.Set;

public interface Template {

    Set<Server> getServers();

    String getName();

    int getMaxPlayers();

    int getMinServers();

    int getMaxServers();

    int getEmptyServers();

    long getRam();

    ServerVersion getVersion();

}
