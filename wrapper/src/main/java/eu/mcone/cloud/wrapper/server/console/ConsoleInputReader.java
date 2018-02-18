/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.wrapper.server.Server;
import lombok.Getter;

import java.util.Scanner;

public abstract class ConsoleInputReader {

    @Getter
    protected Server server;
    @Getter
    protected boolean outputToConsole;

    public ConsoleInputReader(Server server, boolean outputToConsole) {
        this.server = server;
        this.outputToConsole = outputToConsole;

        new Thread(() -> {
            try {
                Scanner sc = new Scanner(server.getProcess().getInputStream());

                while (sc.hasNext()) {
                    String line = sc.nextLine();

                    if (line != null) {
                        String[] lineArray = line.split(" ");
                        this.filter(lineArray, line);

                        if (this.outputToConsole) {
                            Logger.log(getClass(), "[" + this.server.getInfo().getName() + "] >> " + line);
                        }
                    }
                }

                sc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    abstract void filter(String[] lineArray, String line);

}
