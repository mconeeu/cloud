/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.console;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConsoleReader {

    private Map<String, CommandExecutor> executors;

    public ConsoleReader() {
        executors = new HashMap<>();

        new Thread(() -> {
            Scanner sc = new Scanner(System.in);

            while(sc.hasNext()) {
                String next;
                if ((next = sc.nextLine()) != null) {
                    Logger.log(getClass(), "new console input: '"+next+"'");
                    String[] line = next.split(" ");

                    for (HashMap.Entry<String, CommandExecutor> e : executors.entrySet()) {
                        if (e.getKey() == null || e.getKey().equalsIgnoreCase(line[0])) {
                            e.getValue().onCommand(line[0], Arrays.copyOfRange(line, 1, line.length));
                        }
                    }
                }
            }
        }).start();
    }

    public void registerCommand(String cmd, CommandExecutor executor) {
        executors.put(cmd, executor);
    }

    public void registerCommand(CommandExecutor executor) {
        executors.put(null, executor);
    }

}
