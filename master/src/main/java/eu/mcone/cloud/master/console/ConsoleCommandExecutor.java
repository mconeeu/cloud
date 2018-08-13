/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.console;

import eu.mcone.cloud.core.network.packet.ServerCommandExecutePacketWrapper;
import eu.mcone.cloud.core.server.PluginRegisterData;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.network.ChannelPacketHandler;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.core.api.console.CommandExecutor;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;
import eu.mcone.networkmanager.core.console.Logger;

import java.util.Map;
import java.util.UUID;

public class ConsoleCommandExecutor implements CommandExecutor {

    @Override
    public void onCommand(String cmd, String[] args) {
        if (cmd.equalsIgnoreCase("list")) {
            if (args.length == 0) {
                int i = 0;

                Logger.log("List", "Listing all templates");
                System.out.println(ConsoleColor.BLACK_BRIGHT+"--------------------------------------------------------------"+ConsoleColor.RESET);
                System.out.println(ConsoleColor.RED+"TEMPLATE-NAME "+ConsoleColor.YELLOW+"SERVERS "+ConsoleColor.RESET+"RAM "+ConsoleColor.YELLOW+"MIN/MAX "+ConsoleColor.RESET+"VERSION"+ConsoleColor.BLACK_BRIGHT+"                    |"+ConsoleColor.RESET);
                System.out.println(ConsoleColor.BLACK_BRIGHT+"--------------------------------------------------------------"+ConsoleColor.RESET);
                for (Template t : MasterServer.getInstance().getTemplates()) {
                    System.out.println(ConsoleColor.RED+(++i)+". "+t.getName()+" "+ConsoleColor.YELLOW+t.getServers().size()+" "+ConsoleColor.RESET+t.getRam()+" "+ConsoleColor.YELLOW+t.getMin()+"/"+t.getMax()+" "+ConsoleColor.RESET+t.getVersion()+ConsoleColor.RESET);
                }

                i = 0;
                System.out.println("\n");
                Logger.log("List", "Listing all wrappers and servers");
                System.out.println(ConsoleColor.BLACK_BRIGHT+"--------------------------------------------------------------"+ConsoleColor.RESET);
                System.out.println(ConsoleColor.BLUE+"WRAPPER-UUID "+ConsoleColor.YELLOW+"SERVERS "+ConsoleColor.RESET+"RAM "+ConsoleColor.YELLOW+"RAM-IN-USE"+ConsoleColor.BLACK_BRIGHT+"                          |"+ConsoleColor.RESET);
                System.out.println(ConsoleColor.BLACK_BRIGHT+"--------------------------------------------------------------"+ConsoleColor.RESET);
                for (Wrapper w : MasterServer.getInstance().getWrappers()) {
                    int x = 0;

                    System.out.println(ConsoleColor.BLUE+(++i)+". "+w.getUuid()+" "+ConsoleColor.YELLOW+w.getServers().size()+" "+ConsoleColor.RESET+w.getRam()+" "+ConsoleColor.YELLOW+w.getRamInUse()+ConsoleColor.RESET);
                    System.out.println(" └ "+ConsoleColor.GREEN+"SERVER-UUID, SERVER-NAME "+ConsoleColor.YELLOW+"STATE "+ConsoleColor.RESET+"PLAYERS "+ConsoleColor.YELLOW+"IS-STATIC"+ConsoleColor.RESET);
                    for (Server s : w.getServers()) {
                        System.out.println("   "+ConsoleColor.GREEN+(++x)+". "+s.getInfo().getUuid()+", "+s.getInfo().getName()+" "+ConsoleColor.YELLOW+s.getState().toString()+" "+ConsoleColor.RESET+s.getPlayerCount()+"/"+s.getInfo().getMaxPlayers()+" "+ConsoleColor.YELLOW+s.getInfo().isStaticServer()+ConsoleColor.RESET);
                    }
                }

                i = 0;
                System.out.println("\n");
                Logger.log("List", "Listing all offline servers");
                System.out.println(ConsoleColor.BLACK_BRIGHT+"--------------------------------------------------------------"+ConsoleColor.RESET);
                System.out.println(ConsoleColor.GREEN+"SERVER-UUID, SERVER-NAME "+ConsoleColor.YELLOW+"STATE "+ConsoleColor.RESET+"PLAYERS "+ConsoleColor.YELLOW+"IS-STATIC"+ConsoleColor.BLACK_BRIGHT+"             |"+ConsoleColor.RESET);
                System.out.println(ConsoleColor.BLACK_BRIGHT+"--------------------------------------------------------------"+ConsoleColor.RESET);
                for (Server s : MasterServer.getInstance().getServers()) {
                    if (s.getWrapper() == null) {
                        System.out.println(ConsoleColor.GREEN + (++i) + ". " + s.getInfo().getUuid() + ", " + s.getInfo().getName() + " " + ConsoleColor.YELLOW + s.getState().toString() + " " + ConsoleColor.RESET + s.getPlayerCount() + "/" + s.getInfo().getMaxPlayers() + " " + ConsoleColor.YELLOW + s.getInfo().isStaticServer() + ConsoleColor.RESET);
                    }
                }

                if (ChannelPacketHandler.getRegisteringServers() != null) {
                    final Map<UUID, PluginRegisterData> registeringServers = ChannelPacketHandler.getRegisteringServers();

                    System.out.println("");
                    Logger.log("List", ConsoleColor.RED + "[!] Current registering servers with which are not known: " +registeringServers.size());
                    Logger.log("List", ConsoleColor.RED + registeringServers);
                }

                System.out.println("");
                Logger.log("List", "-- end");
                return;
            }
        } else if (cmd.equalsIgnoreCase("cmd")) {
            if (args.length >= 2) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    StringBuilder sb = new StringBuilder();

                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                        if (i != args.length - 1) sb.append(" ");
                    }

                    s.getWrapper().getChannel().writeAndFlush(new ServerCommandExecutePacketWrapper(s.getInfo().getUuid(), sb.toString()));
                    System.out.println("Sent new command '" + sb.toString() + "' to server wrapper...");
                } else {
                    Logger.log(getClass(), ConsoleColor.RED+"No suitable server found for name " + args[0]);
                }
                return;
            }
        } else if (cmd.equalsIgnoreCase("startserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.start(); 
                } else {
                    Logger.log(getClass(), ConsoleColor.RED+"The server "+args[0]+" does not exist!");
                }
                return;
            }
        } else if (cmd.equalsIgnoreCase("stopserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.stop();
                } else {
                    Logger.log(getClass(), ConsoleColor.RED+"The server "+args[0]+" does not exist!");
                }
                return;
            }
        } else if (cmd.equalsIgnoreCase("forcestopserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.forcestop();
                } else {
                    Logger.log(getClass(), ConsoleColor.RED+"Dieser Server existiert nicht!");
                }
                return;
            }
        } else if (cmd.equalsIgnoreCase("reload")) {
            if (args.length == 0) {
                MasterServer.getInstance().reload();
                return;
            }
        }

        Logger.log(getClass(), ConsoleColor.RED+cmd+" is not a valid command!");
    }

}
