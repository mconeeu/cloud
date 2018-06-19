/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.dsme;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.dsme.server.ServerBootstrap;
import lombok.Getter;

public class DSME {

    @Getter
    private DSME instance;
    @Getter
    private ServerBootstrap serverBootstrap;

    public static void main(String[] args) {
        new DSME();
    }

    private DSME() {
        Logger.log(getClass(), "Starting Dahsboard Server Managmend Engine");
        this.instance = this;

        Logger.log(getClass(), "Starting Boostrap Server...");
        this.serverBootstrap = new ServerBootstrap(3000);
    }
}
