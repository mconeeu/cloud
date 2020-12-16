/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.api.plugin.bungee;

import eu.mcone.cloud.api.plugin.CloudPlugin;
import net.md_5.bungee.api.config.ServerInfo;

public interface BungeeCloudPlugin extends CloudPlugin {

    ServerInfo getFallbackServer();

}
