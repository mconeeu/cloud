/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class ConsoleInputReader {

    ConsoleInputReader(Server server) {
        new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(server.getProcess().getInputStream()));

            String line;
            try {
                while ((line = br.readLine()) != null) {
                    System.out.println("[" + server.getInfo().getName() + "] >> " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /*
    private void LogConsole(Server server, String input){
        try{
            Properties ps = new Properties();

            InputStreamReader inputstreamreader = new InputStreamReader(Files.newInputStream(Paths.get(Var.getSystemPath() + "\\wrapper\\servers\\" + server.getInfo().getName() + "\\mc1cloud\\Console-" + server.getCurrent_date() + ".yml")));
            ps.load(inputstreamreader);

            ps.setProperty("[" + server.getInfo().getName() + "]", input);

            OutputStream outputstream = Files.newOutputStream(Paths.get(Var.getSystemPath() + "\\wrapper\\servers\\" + server.getInfo().getName() + "\\mc1cloud\\Console-" + server.getCurrent_date() + ".yml"));
            ps.store(outputstream, "MCONE_WRAPPER");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    */
}
