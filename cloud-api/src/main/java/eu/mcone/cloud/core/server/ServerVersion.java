/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.server;

import lombok.Getter;

public enum ServerVersion {
    SPIGOT_LATEST("https://cdn.getbukkit.org/spigot/spigot-1.8.8-R0.1-SNAPSHOT-latest.jar"),
    SPIGOT("https://cdn.getbukkit.org/spigot/spigot-1.13.2.jar"),
    BUNGEE("https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar");

    @Getter
    private String downloadLink;

    ServerVersion(String downloadLink) {
        this.downloadLink = downloadLink;
    }

}
