/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import eu.mcone.cloud.plugin.CloudPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin {

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
