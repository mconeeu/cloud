/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.download;

import lombok.Getter;

public enum CiServer {

    MCONE("https://ci.mcone.eu", "cloudsystem", "UZuV0qgQuIxsgp3W");

    @Getter
    private String uri, user, password;

    CiServer(String uri, String user, String password) {
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

}
