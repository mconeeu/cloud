/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.wrapper.server.Server;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public abstract class ConsoleInputReader {

    @Getter
    protected Server server;

    public ConsoleInputReader(Server server, boolean filter) {
        this.server = server;

        new Thread(() -> {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(server.getProcess().getInputStream()));

                String line = br.readLine();
                String[] lineArray;

                while (line != null) {
                    if (filter) {
                        lineArray = br.readLine().split(" ");
                        this.filter(lineArray, line);
                    } else {
                        line = br.readLine();
                        System.out.println("[" + this.server.getInfo().getName() + "] >> " + line);
                    }
                }

                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    abstract void filter(String[] lineArray, String line);

}
