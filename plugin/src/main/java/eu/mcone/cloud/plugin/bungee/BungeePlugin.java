/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bungee;

import eu.mcone.cloud.plugin.CloudPlugin;
import eu.mcone.cloud.plugin.network.ClientBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePlugin extends Plugin {

    private CloudPlugin instance;

    @Override
    public void onEnable() {
        instance = new CloudPlugin();
    }

    @Override
    public void onDisable() {
        instance.unload();
    }

}
