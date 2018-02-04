/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import eu.mcone.cloud.core.network.packet.ServerUpdateStatePacketPlugin;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.plugin.CloudPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin {

    private CloudPlugin instance;

    @Override
    public void onLoad() {
        instance = new CloudPlugin();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this.instance), this);
        Bukkit.getScheduler().runTask(this, () -> instance.send(new ServerUpdateStatePacketPlugin(instance.getServerUuid(), ServerState.RUNNING)));
    }

    @Override
    public void onDisable() {
        instance.send(new ServerUpdateStatePacketPlugin(instance.getServerUuid(), ServerState.STOPPED));
        instance.unload();
    }

}
