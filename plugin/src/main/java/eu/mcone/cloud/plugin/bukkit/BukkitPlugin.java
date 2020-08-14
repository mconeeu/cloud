/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import eu.mcone.cloud.api.plugin.bukkit.BukkitCloudPlugin;
import eu.mcone.cloud.plugin.CloudPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BukkitPlugin extends JavaPlugin implements BukkitCloudPlugin {

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
        instance.unload();
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
    }

    @Override
    public Map<UUID, String> getPlayers() {
        Map<UUID, String> result = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            result.put(p.getUniqueId(), p.getName());
        }

        return result;
    }

}
