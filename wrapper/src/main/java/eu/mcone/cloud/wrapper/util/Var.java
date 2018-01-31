/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.util;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.nio.channels.Channel;
import java.util.ArrayList;

public class Var {

    @Getter
    private static ArrayList<ChannelHandlerContext> connections = new ArrayList<>();

    public static final String home_directory = System.getProperty("user.home");

}
