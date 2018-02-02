/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.console;

import eu.mcone.cloud.core.network.packet.ServerResultPacket;
import eu.mcone.cloud.wrapper.server.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleErrorStreamReader {

    public ConsoleErrorStreamReader() {}

    public ConsoleErrorStreamReader(Server sr) {
        new Thread(() -> {
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(sr.getP().getErrorStream()));
                StringBuilder builder = new StringBuilder();
                String line = reader.readLine();

                while (line != null) {
                    builder.append(line);
                    line = reader.readLine();

                    if(!(line == null)){
                        System.out.println("[ErrorStream] Server: '" + sr.getInfo().getName() + "] >> " + line.toString());
                        sr.sendResult("[Server." + sr.getInfo().getName() + "] " + line.toString(), ServerResultPacket.Result.SERVER_ERROR);
                    }else{return;}

                }
                reader.close();
            }catch(Exception e){
                e.printStackTrace();
            }

        }).start();
        return;
    }
}
