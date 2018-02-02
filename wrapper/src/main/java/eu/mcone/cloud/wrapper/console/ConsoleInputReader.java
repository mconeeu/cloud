/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.console;

import eu.mcone.cloud.core.network.packet.ServerResultPacket;
import eu.mcone.cloud.core.system.OS;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.cloud.wrapper.util.Var;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ConsoleInputReader {

    @lombok.Getter
    private String[] line_array;

    @lombok.Getter
    private String line_normal;

    @lombok.Getter
    private Server sr;

    public ConsoleInputReader(Server sr, Boolean use_filter) {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(sr.getP().getInputStream()));
                StringBuilder builder = new StringBuilder();

                String line_normal = reader.readLine();
                String[] line_array = line_normal.split(" ");

                builder.append(line_normal);

                this.sr = sr;
                this.line_normal = line_normal;
                this.line_array = line_array;

                while (line_normal != null) {
                    if (use_filter == Boolean.TRUE) {
                        this.line_array = reader.readLine().split(" ");
                        this.ConsoleFilter();
                    } else {
                        this.line_normal = reader.readLine();
                        System.out.println("[" + this.sr.getInfo().getName() + "] >> " + line_normal.toString());
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
        return;
    }

    private void ConsoleFilter() {
        if (this.line_array[2].equalsIgnoreCase("Done")) {
            if (!(this.line_normal == null)) {
               String message = String.join(" ", this.line_array);
               System.out.println("[" + this.sr.getInfo().getName() + " >> " + message.toString());
               this.sr.sendResult("[Server." + sr.getInfo().getName() + "] The server started successfully...", ServerResultPacket.Result.SUCCESSFUL);
            } else {
                return;
            }
        } else if (this.line_array[2].equalsIgnoreCase("")) {

        } else {return;}
    }

    /*
    private void LogConsole(Server sr, String input){
        try{
            Properties ps = new Properties();

            InputStreamReader inputstreamreader = new InputStreamReader(Files.newInputStream(Paths.get(Var.getSystemPath() + "\\wrapper\\servers\\" + sr.getInfo().getName() + "\\mc1cloud\\Console-" + sr.getCurrent_date() + ".yml")));
            ps.load(inputstreamreader);

            ps.setProperty("[" + sr.getInfo().getName() + "]", input);

            OutputStream outputstream = Files.newOutputStream(Paths.get(Var.getSystemPath() + "\\wrapper\\servers\\" + sr.getInfo().getName() + "\\mc1cloud\\Console-" + sr.getCurrent_date() + ".yml"));
            ps.store(outputstream, "MCONE_WRAPPER");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    */
}
