/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.network.packet.ServerProgressStatePacketMaster;
import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

class ConsoleInputReaderBungee {

    @Getter
    private Server bungee;

    ConsoleInputReaderBungee(Server bungee, Boolean use_filter) {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(bungee.getProcess().getInputStream()));
                StringBuilder builder = new StringBuilder();

                String line_normal = reader.readLine();
                String[] line_array;

                builder.append(line_normal);

                this.bungee = bungee;

                while (line_normal != null) {
                    if (use_filter == Boolean.TRUE) {
                        line_array = reader.readLine().split(" ");
                        this.ConsoleFilter(line_array, line_normal);
                    } else {
                        line_normal = reader.readLine();
                        System.out.println("[" + this.bungee.getInfo().getName() + "] >> " + line_normal);
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
        return;
    }


    private void ConsoleFilter(String[] lineArray, String lineNormal) {
        if (lineArray[2].equalsIgnoreCase("Done")) {
            if (!(lineNormal == null)) {
                //Server is finishing
                String message = String.join(" ", lineArray);
                System.out.println("[" + this.bungee.getInfo().getName() + " >> " + message.toString());
                this.bungee.sendResult("[Bungee." + bungee.getInfo().getName() + "] The server started successfully...", ServerResultPacketWrapper.Result.SUCCESSFUL);
            } else {
                return;
            }
        }
    }
}
