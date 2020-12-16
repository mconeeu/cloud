/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.api.template.Template;
import eu.mcone.cloud.core.api.wrapper.Wrapper;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.template.CloudTemplate;
import eu.mcone.cloud.master.wrapper.CloudWrapper;
import group.onegaming.networkmanager.core.api.console.ConsoleColor;
import lombok.extern.java.Log;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log
public class ServerManager {

    private ScheduledExecutorService es;

    //creates or deletes empty servers for or from templates and tries to start servers from serverWaitList
    public ServerManager() {
        es = Executors.newSingleThreadScheduledExecutor();
        es.scheduleAtFixedRate(() -> {
            LinkedHashSet<CloudServer> serverWaitList = new LinkedHashSet<>();

            for (Template t : MasterServer.getServer().getTemplates()) {
                HashMap<CloudServer, Integer> playercount = new HashMap<>();

                for (Server s : t.getServers()) {
                    CloudServer server = (CloudServer) s;

                    //Add to ServerWaitlist if Wrapper == null
                    if (server.getWrapper() == null && !server.isPreventStart()) {
                        serverWaitList.add(server);
                    }

                    //Save actual playercount from every server in an hashmap
                    if (server.getPlayerCount() >= 0) {
                        playercount.put(server, server.getPlayerCount());
                    }
                }

                int emptycount = 0;

                for (Map.Entry<CloudServer, Integer> e : playercount.entrySet()) {
                    //Count the amount of servers with 0 players.
                    if (e.getValue() < (0.4 * e.getKey().getTemplate().getMaxServers())) {
                        emptycount++;
                    }
                }

                //If the amount of empty servers is smaller then the set amount, create more empty servers
                if (emptycount < t.getEmptyServers()) {
                    //If the maximum server count is not reached after adding server, create server
                    if (t.getServers().size() + 1 <= t.getMaxServers()) {
                        ((CloudTemplate) t).createServer(1);
                    }
                    //Else if the amount of empty servers is bigger then the set amount, delete empty servers
                } else if (emptycount > t.getEmptyServers() && emptycount > 1) {
                    int deleteServers = emptycount - t.getEmptyServers();

                    for (Server s : t.getServers()) {
                        if (s.getPlayerCount() < (0.4 * s.getTemplate().getMaxServers()) && deleteServers > 0) {
                            deleteServers--;

                            //If the minimum server count is not reached after deleting server, delete server.
                            if (t.getServers().size() - 1 >= t.getMinServers()) {
                                s.delete();
                            }
                        }
                    }
                }
            }

            for (CloudServer s : MasterServer.getServer().getStaticServerManager().getServers()) {
                if (s.getWrapper() == null && !s.isPreventStart()) {
                    serverWaitList.add(s);
                }
            }

            Iterator<CloudServer> i = serverWaitList.iterator();

            while (i.hasNext()) {
                CloudServer server = i.next();

                if (!server.getInfo().isStaticServer()) {
                    CloudWrapper bestwrapper = getBestWrapper();

                    if (bestwrapper != null) {
                        server.setWrapper(bestwrapper);
                        i.remove();
                        log.info(ConsoleColor.GREEN + "Found wrapper " + bestwrapper.getUuid() + " for server " + server.getInfo().getName() + "! Creating Server!" + ConsoleColor.RESET);

                        try {
                            bestwrapper.createServer(server).await();
                            server.start();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        log.finest(ConsoleColor.DARK_GRAY + "No wrapper for server " + server.getInfo().getName() + " available! Staying in WaitList..." + ConsoleColor.RESET);
                    }
                } else {
                    CloudWrapper wrapper = (CloudWrapper) MasterServer.getServer().getWrapper(server.getWrapperUuid());

                    if (wrapper != null && !wrapper.isBusy()) {
                        server.setWrapper(wrapper);
                        i.remove();
                        log.info(ConsoleColor.GREEN + "Found explicit wrapper " + wrapper.getUuid() + " for server " + server.getInfo().getName() + "! Creating Server!");

                        try {
                            wrapper.createServer(server).await();
                            server.start();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        log.finest(ConsoleColor.DARK_GRAY + "Explicit wrapper " + server.getWrapperUuid() + " not found for server " + server.getInfo().getName() + "! Staying in WaitList..." + ConsoleColor.RESET);
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    //Returns the best wrapper with less ram
    private CloudWrapper getBestWrapper() {
        HashMap<CloudWrapper, Long> wrappers = new HashMap<>();

        for (Wrapper w : MasterServer.getServer().getWrappers()) {
            if (!w.isBusy()) {
                long difference = w.getRam() - w.getRamInUse();

                //Exclude wrappers which ram is nearly full
                if (difference > 100) {
                    wrappers.put((CloudWrapper) w, w.getRam() - w.getRamInUse());
                }
            }
        }

        if (wrappers.isEmpty()) {
            return null;
        } else {
            //Return the wrapper with less ram
            return Collections.min(wrappers.entrySet(), HashMap.Entry.comparingByValue()).getKey();
        }
    }

    public void shutdown() {
        es.shutdown();
    }

}
