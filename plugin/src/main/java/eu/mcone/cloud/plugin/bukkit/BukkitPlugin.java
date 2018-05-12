/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import eu.mcone.cloud.api.plugin.bukkit.BukkitCloudPlugin;
import eu.mcone.cloud.core.server.CloudWorld;
import eu.mcone.cloud.plugin.CloudPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements BukkitCloudPlugin {

    private CloudPlugin instance;

    @Override
    public void onEnable() {
        instance = new CloudPlugin(this);

        for (CloudWorld world : instance.getLoadedWorlds()) {
            if (Bukkit.getWorld(world.getName()) == null) {
                WorldCreator wc = new WorldCreator(world.getName())
                        .environment(World.Environment.valueOf(world.getEnvironment()))
                        .type(WorldType.valueOf(world.getWorldType()))
                        .generateStructures(world.isGenerateStructures());

                if (world.getGenerator() != null) {
                    wc.generator(world.getGenerator());
                    if (world.getGeneratorSettings() != null) wc.generatorSettings(world.getGeneratorSettings());
                }

                wc.createWorld();
            }
        }

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
    public int getPlayerCount() {
        return Bukkit.getOnlinePlayers().size();
    }

}
