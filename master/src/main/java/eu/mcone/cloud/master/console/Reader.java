/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.console;

import eu.mcone.cloud.core.network.packet.ServerCommandExecutePacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.template.Template;
import io.netty.buffer.ByteBuf;
import javafx.scene.layout.Priority;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.UUID;

public class Reader {

    public Reader(){
        new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                try {
                    String[] line = reader.readLine().split(" ");
                    if (line[0].equalsIgnoreCase("cmd")) {
                        for (Template t : MasterServer.templates.values()) {
                            for (Server s : t.getServers().values()) {
                                if (s.getInfo().getName().equalsIgnoreCase(line[1])) {
                                    s.getWrapper().getChannel().writeAndFlush(new ServerCommandExecutePacket(s.getInfo().getUuid(), line[2]));
                                    System.out.println("Sent new command '" + line[2] + "' to server wrapper...");
                                    return;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
