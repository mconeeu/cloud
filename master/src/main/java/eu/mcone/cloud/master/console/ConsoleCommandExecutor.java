/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.console;

import eu.mcone.cloud.core.packet.ServerCommandExecutePacketWrapper;
import eu.mcone.cloud.core.server.PluginRegisterData;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.handler.WrapperRegisterFromStandaloneHandler;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.core.api.console.CommandExecutor;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class ConsoleCommandExecutor implements CommandExecutor {

    private final static Logger log = Logger.getLogger("commandExecutor");

    @Override
    public void onCommand(String cmd, String[] args) {
        if (cmd.equalsIgnoreCase("list")) {
            if (args.length == 0) {
                int i = 0;

                log.info("List - Listing all templates");
                log.info(ConsoleColor.DARK_GRAY + "--------------------------------------------------------------");
                log.info(ConsoleColor.RED + "TEMPLATE-NAME " + ConsoleColor.YELLOW + "SERVERS " + ConsoleColor.RESET + "RAM " + ConsoleColor.YELLOW + "MIN/MAX " + ConsoleColor.RESET + "VERSION" + ConsoleColor.DARK_GRAY + "                    |");
                log.info(ConsoleColor.DARK_GRAY + "--------------------------------------------------------------");
                for (Template t : MasterServer.getInstance().getTemplates()) {
                    log.info(ConsoleColor.RED.toString() + (++i) + ". " + t.getName() + " " + ConsoleColor.YELLOW + t.getServers().size() + " " + ConsoleColor.RESET + t.getRam() + " " + ConsoleColor.YELLOW + t.getMin() + "/" + t.getMax() + " " + ConsoleColor.RESET + t.getVersion());
                }

                i = 0;
                log.info("\n");
                log.info("List - Listing all wrappers and servers");
                log.info(ConsoleColor.DARK_GRAY + "--------------------------------------------------------------");
                log.info(ConsoleColor.BLUE + "WRAPPER-UUID " + ConsoleColor.YELLOW + "SERVERS " + ConsoleColor.RESET + "RAM " + ConsoleColor.YELLOW + "RAM-IN-USE" + ConsoleColor.DARK_GRAY + "                          |");
                log.info(ConsoleColor.DARK_GRAY + "--------------------------------------------------------------");
                for (Wrapper w : MasterServer.getInstance().getWrappers()) {
                    int x = 0;

                    log.info(ConsoleColor.BLUE.toString() + (++i) + ". " + w.getUuid() + " " + ConsoleColor.YELLOW + w.getServers().size() + " " + ConsoleColor.RESET + w.getRam() + " " + ConsoleColor.YELLOW + w.getRamInUse());
                    log.info(" └ " + ConsoleColor.GREEN + "SERVER-UUID, SERVER-NAME " + ConsoleColor.YELLOW + "STATE " + ConsoleColor.RESET + "PLAYERS " + ConsoleColor.YELLOW + "IS-STATIC");
                    for (Server s : w.getServers()) {
                        log.info("   " + ConsoleColor.GREEN + (++x) + ". " + s.getInfo().getUuid() + ", " + s.getInfo().getName() + " " + ConsoleColor.YELLOW + s.getState().toString() + " " + ConsoleColor.RESET + s.getPlayerCount() + "/" + s.getInfo().getMaxPlayers() + " " + ConsoleColor.YELLOW + s.getInfo().isStaticServer());
                    }
                }

                i = 0;
                log.info("\n");
                log.info("List - Listing all offline servers");
                log.info(ConsoleColor.DARK_GRAY + "--------------------------------------------------------------");
                log.info(ConsoleColor.GREEN + "SERVER-UUID, SERVER-NAME " + ConsoleColor.YELLOW + "STATE " + ConsoleColor.RESET + "PLAYERS " + ConsoleColor.YELLOW + "IS-STATIC" + ConsoleColor.DARK_GRAY + "             |");
                log.info(ConsoleColor.DARK_GRAY + "--------------------------------------------------------------");
                for (Server s : MasterServer.getInstance().getServers()) {
                    if (s.getWrapper() == null) {
                        log.info(ConsoleColor.GREEN.toString() + (++i) + ". " + s.getInfo().getUuid() + ", " + s.getInfo().getName() + " " + ConsoleColor.YELLOW + s.getState().toString() + " " + ConsoleColor.RESET + s.getPlayerCount() + "/" + s.getInfo().getMaxPlayers() + " " + ConsoleColor.YELLOW + s.getInfo().isStaticServer());
                    }
                }

                log.info("\n");
                if (WrapperRegisterFromStandaloneHandler.getRegisteringServers() != null) {
                    final Map<UUID, PluginRegisterData> registeringServers = WrapperRegisterFromStandaloneHandler.getRegisteringServers();

                    log.info("List"+ConsoleColor.RED + "[!] Current registering servers with which are not known: " + registeringServers.size());
                    log.info("List"+ConsoleColor.RED + registeringServers);
                }

                log.info("\n");
                log.info("List - end");
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
                    log.info("Sent new command '" + sb.toString() + "' to server wrapper...");
                } else {
                    log.info(ConsoleColor.RED + "No suitable server found for name " + args[0]);
                }
                return;
            }
        } else if (cmd.equalsIgnoreCase("startserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.start();
                } else {
                    log.info(ConsoleColor.RED + "The server " + args[0] + " does not exist!");
                }
                return;
            }
        } else if (cmd.equalsIgnoreCase("stopserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.stop();
                } else {
                    log.info(ConsoleColor.RED + "The server " + args[0] + " does not exist!");
                }
                return;
            }
        } else if (cmd.equalsIgnoreCase("forcestopserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.forcestop();
                } else {
                    log.info(ConsoleColor.RED + "Dieser Server existiert nicht!");
                }
                return;
            }
        } else if (cmd.equalsIgnoreCase("reload")) {
            if (args.length == 0) {
                MasterServer.getInstance().reload();
                return;
            }
        }

        log.info(ConsoleColor.RED + cmd + " is not a valid command!");
    }

}
