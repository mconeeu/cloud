/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.api.plugin;

import java.util.Map;
import java.util.UUID;

public interface CloudPlugin {

    void runAsync(Runnable runnable);

    Map<UUID, String> getPlayers();

}
