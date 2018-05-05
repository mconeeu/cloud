/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class CloudTemplate {

    String name;

    int serverCount;
    int maxPlayers;
    int min;
    int max;
    int emptyservers;

    List<String> servers;

}
