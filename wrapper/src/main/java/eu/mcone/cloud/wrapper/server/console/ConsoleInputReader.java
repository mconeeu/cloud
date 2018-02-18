/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.wrapper.server.Server;
import lombok.Getter;

import java.util.Scanner;

public abstract class ConsoleInputReader {

    @Getter
    protected Server server;

    ConsoleInputReader(Server server, boolean outputConsole) {
        this.server = server;

        new Thread(() -> {
            try {
                Scanner sc = new Scanner(server.getProcess().getInputStream());

                while (sc.hasNext()) {
                    String line = sc.nextLine();

                    if (line != null) {
                        String[] line_array = line.split(" ");
                        this.filter(line_array, line);

                        if (outputConsole) {
                            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                                //String console = sc.nextLine().replace("\n", System.getProperty("line.separator"));

                                System.out.println("[" + this.server.getInfo().getName() + "] >> " + line);
                            } else {
                                System.out.println("[" + this.server.getInfo().getName() + "] >> " + line);
                            }
                        }
                    }
                }

                //sc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    abstract void filter(String[] lineArray, String line);

}
