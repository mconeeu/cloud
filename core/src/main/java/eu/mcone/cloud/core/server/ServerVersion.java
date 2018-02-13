/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.server;

import lombok.Getter;

public enum ServerVersion {
    BUKKIT(""),
    SPIGOT(""),
    BUNGEE(""),
    FORGE(""),
    CAULDRON("");

    @Getter
    private String downloadLink;

    ServerVersion(String downloadLink) {
        this.downloadLink = downloadLink;
    }

}
