/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import eu.mcone.cloud.core.network.packet.ServerUpdateStatePacketWrapper;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.plugin.CloudPlugin;
import eu.mcone.cloud.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements Plugin {

    private CloudPlugin instance;

    @Override
    public void onLoad() {
        instance = new CloudPlugin(this);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this.instance), this);
    }

    @Override
    public void onDisable() {
        instance.send(new ServerUpdateStatePacketWrapper(instance.getServerUuid(), ServerState.OFFLINE));
        instance.unload();
    }

}
