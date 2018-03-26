/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.server;

import lombok.Getter;

public enum ServerVersion {
    BUKKIT("https://yivesmirror.com/files/craftbukkit/craftbukkit-latest.jar"),
    SPIGOT_LATEST("https://yivesmirror.com/files/paperspigot/PaperSpigot-latest.jar"),
    SPIGOT("https://yivesmirror.com/files/spigot/spigot-1.8.8-R0.1-SNAPSHOT.jar"),
    BUNGEE("https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar"),
    FORGE(""),
    CAULDRON("https://yivesmirror.com/files/cauldron/cauldron-1.7.10-2.1403.1.54.zip");

    @Getter
    private String downloadLink;

    ServerVersion(String downloadLink) {
        this.downloadLink = downloadLink;
    }

}
