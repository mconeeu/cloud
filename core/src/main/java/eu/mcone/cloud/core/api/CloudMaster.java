/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class CloudMaster {

    List<CloudTemplate> templates;
    List<CloudWrapper> wrappers;
    List<CloudServer> servers;

}
