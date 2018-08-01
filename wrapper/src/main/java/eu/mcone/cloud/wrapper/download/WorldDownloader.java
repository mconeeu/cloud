/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.download;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.exception.CloudException;
import eu.mcone.cloud.core.server.CloudWorld;
import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

public class WorldDownloader {

    @Getter
    private String name;
    private final static String worldPath = WrapperServer.getInstance().getFileManager().getHomeDir() + File.separator + "worlds";

    public WorldDownloader(String name) {
        this.name = name;
    }

    public CloudWorld download() {
        return WrapperServer.getInstance().getMySQL().select("SELECT `build` FROM `" + WrapperServer.getInstance().getMySQL().getTablePrefix() + "_worlds` WHERE `name`='" + name + "'", rs -> {
            try {
                if (rs.next()) {
                    File zipFile = new File(worldPath + File.separator + name + ".zip");

                    int oldBuild = WrapperServer.getInstance().getConfig().getConfig().getSection("builds").getSection("worlds").getInt(name);
                    int build = rs.getInt("build");

                    if (build != oldBuild) {
                        return WrapperServer.getInstance().getMySQL().select("SELECT * FROM `" + WrapperServer.getInstance().getMySQL().getTablePrefix() + "_worlds` WHERE `name`='" + name + "'", rs1 -> {
                            try {
                                if (rs1.next()) {
                                    Logger.log(getClass(), "Downloading World " + name + "...");
                                    FileOutputStream fos = new FileOutputStream(zipFile);
                                    fos.write(rs1.getBytes("bytes"));
                                    fos.close();

                                    WrapperServer.getInstance().getConfig().getConfig().set("builds.worlds." + name, build);
                                    WrapperServer.getInstance().getConfig().save();

                                    return new CloudWorld(
                                            name,
                                            zipFile.getPath()
                                    );
                                }
                            } catch (SQLException | IOException e) {
                                e.printStackTrace();
                            }

                            return null;
                        }, CloudWorld.class);
                    } else {
                        return new CloudWorld(
                                name,
                                zipFile.getPath()
                        );
                    }
                } else {
                    throw new CloudException("World " + name + " could not be found in Database!");
                }
            } catch (SQLException | CloudException e) {
                e.printStackTrace();
            }
            return null;
        }, CloudWorld.class);
    }
}
