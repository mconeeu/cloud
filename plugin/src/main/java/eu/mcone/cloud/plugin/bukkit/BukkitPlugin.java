/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.bukkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import eu.mcone.cloud.core.server.world.CloudWorld;
import eu.mcone.cloud.plugin.CloudPlugin;
import eu.mcone.cloud.api.plugin.Plugin;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements Plugin {

    private CloudPlugin instance;

    @Override
    public void onEnable() {
        instance = new CloudPlugin(this);

        for (CloudWorld world : instance.getLoadedWorlds()) {
            if (Bukkit.getWorld(world.getName()) == null) {
                WorldCreator wc = new WorldCreator(world.getName())
                        .environment(World.Environment.valueOf(world.getEnvironment()))
                        .type(WorldType.valueOf(world.getWorldType()))
                        .generateStructures(world.getProperties().isGenerateStructures());

                if (world.getGenerator() != null) wc.generator(world.getGenerator());

                World w = wc.createWorld();
                w.setDifficulty(Difficulty.valueOf(world.getDifficulty()));
                JsonArray loc = new JsonParser().parse(world.getSpawnLocation()).getAsJsonArray();
                w.setSpawnLocation(loc.get(0).getAsInt(), loc.get(1).getAsInt(), loc.get(2).getAsInt());
                w.setPVP(world.getProperties().isPvp());
                w.setKeepSpawnInMemory(world.getProperties().isKeepSpawnInMemory());

                if (!world.getProperties().isAllowAnimals()) {
                    w.setAnimalSpawnLimit(0);
                    w.setWaterAnimalSpawnLimit(0);
                }
                if (!world.getProperties().isAllowMonsters()) {
                    w.setMonsterSpawnLimit(0);
                }
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
