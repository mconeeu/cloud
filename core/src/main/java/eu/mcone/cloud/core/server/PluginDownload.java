/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PluginDownload {
    @Getter
    private String ciServer, job, artifact;
}
