package eu.mcone.cloud.master.console;

import eu.mcone.cloud.core.network.packet.ServerCommandExecutePacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.template.Template;
import javafx.scene.layout.Priority;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Created with IntelliJ IDE
 * Created on 28.01.2018
 * Copyright (c) 2018 Dominik L. All rights reserved
 * You are not allowed to decompile the code
 */

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
                                    System.out.println(MasterServer.connections.get(0).channel().remoteAddress().toString());
                                    MasterServer.connections.get(0).channel().writeAndFlush(new ServerCommandExecutePacket(s.getInfo().getUuid(), line[2]));
                                    System.out.println("sent new msg");
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
