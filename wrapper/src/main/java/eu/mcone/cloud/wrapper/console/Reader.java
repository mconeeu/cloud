/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.console;

import eu.mcone.cloud.wrapper.WrapperServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Reader {

    public Reader() {
        new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                try {
                    String[] line = reader.readLine().split(" ");

                    if (line[0].equalsIgnoreCase("\\wrapper")) {
                        if (line[1].equalsIgnoreCase("help")) {
                            System.out.println("-----> Wrapper Commands <-----\n" +
                                    "\\wrapper get version, returns the current version of the wrapper\n" +
                                    "\\warpper get local channeladdress, return the localChannelAddress of the Server");

                        }else if (line[1].equalsIgnoreCase("get")) {
                            if(line[2].equalsIgnoreCase("version")){
                                System.out.println("Wrapper current version: " + WrapperServer.getInstance().getCurrentVersion());
                            }else if (line[2].equalsIgnoreCase("local")) {
                                if (line[3].equalsIgnoreCase("channeladdress")) {
                                    String localChannelAddress = WrapperServer.getInstance().getChannel().remoteAddress().toString();
                                    System.out.println("Wrapper localChannelAddress: " + localChannelAddress);
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
