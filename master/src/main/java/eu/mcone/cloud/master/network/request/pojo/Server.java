/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network.request.pojo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Server {

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
