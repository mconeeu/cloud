/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network.request.pojo;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class Master {

    List<Template> templates;
    List<Wrapper> wrappers;
    List<Server> servers;

}
