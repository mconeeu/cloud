/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CloudServer {

    String name;
    String template;
    String wrapper;
    String hostname;
    String state;
    String version;

    int port;
    int onlinePlayers;
    int playerCount;

    long ram;

}
