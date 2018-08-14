/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.download;

import com.mongodb.MongoException;
import eu.mcone.cloud.core.server.CloudWorld;
import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.Getter;
import lombok.extern.java.Log;
import org.bson.Document;
import org.bson.types.Binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;

@Log
public class WorldDownloader {

    @Getter
    private String name;
    private final static String worldPath = WrapperServer.getInstance().getFileManager().getHomeDir() + File.separator + "worlds";

    public WorldDownloader(String name) {
        this.name = name;
    }

    public CloudWorld download() throws MongoException {
        log.fine("Collecting database information about world " + name + "...");
        Document buildEntry = WrapperServer.getInstance().getMongoDB().getCollection("cloudwrapper_worlds").find(eq("name", name)).projection(include("build")).first();

        File zipFile = new File(worldPath + File.separator + name + ".zip");

        int oldBuild = WrapperServer.getInstance().getConfig().getConfig().getSection("builds").getSection("worlds").getInt(name);
        int build = buildEntry.getInteger("build");

        if (build != oldBuild) {
            Document worldEntry = WrapperServer.getInstance().getMongoDB().getCollection("cloudwrapper_worlds").find(eq("name", name)).first();

            try {
                log.info("Downloading World " + name + "...");
                FileOutputStream fos = new FileOutputStream(zipFile);
                fos.write(worldEntry.get("bytes", Binary.class).getData());
                fos.close();

                WrapperServer.getInstance().getConfig().getConfig().set("builds.worlds." + name, build);
                WrapperServer.getInstance().getConfig().save();

                return new CloudWorld(
                        name,
                        zipFile.getPath()
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        } else {
            return new CloudWorld(
                    name,
                    zipFile.getPath()
            );
        }
    }
}
