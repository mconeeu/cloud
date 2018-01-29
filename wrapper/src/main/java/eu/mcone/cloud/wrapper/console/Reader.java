package eu.mcone.cloud.wrapper.console;

import eu.mcone.cloud.wrapper.WrapperServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDE
 * Created on 28.01.2018
 * Copyright (c) 2018 Dominik L. All rights reserved
 * You are not allowed to decompile the code
 */

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
                                System.out.println("Wrapper current version: " + WrapperServer.current_version);
                            }else if (line[2].equalsIgnoreCase("local")) {
                                if (line[3].equalsIgnoreCase("channeladdress")) {
                                    String localChannelAddress = WrapperServer.connections.get(0).channel().localAddress().toString();
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
