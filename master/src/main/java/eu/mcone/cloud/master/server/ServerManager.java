/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;
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
            LinkedHashSet<Server> serverWaitList = new LinkedHashSet<>();

            for (Template t : MasterServer.getInstance().getTemplates()) {
                HashMap<Server, Integer> playercount = new HashMap<>();

                for (Server s : t.getServers()) {
                    //Add to ServerWaitlist if Wrapper == null
                    if (s.getWrapper() == null && !s.isPreventStart()) {
                        serverWaitList.add(s);
                    }

                    //Save actual playercount from every server in an hashmap
                    if (s.getPlayerCount() >= 0) {
                        playercount.put(s, s.getPlayerCount());
                    }
                }

                int emptycount = 0;

                for (Map.Entry<Server, Integer> e : playercount.entrySet()) {
                    //Count the amount of servers with 0 players.
                    if (e.getValue() < (0.4 * e.getKey().getTemplate().getMax())) {
                        emptycount++;
                    }
                }

                //If the amount of empty servers is smaller then the set amount, create more empty servers
                if (emptycount < t.getEmptyServers()) {
                    //If the maximum server count is not reached after adding server, create server
                    if (t.getServers().size() + 1 <= t.getMax()) {
                        t.createServer(1);
                    }
                    //Else if the amount of empty servers is bigger then the set amount, delete empty servers
                } else if (emptycount > t.getEmptyServers() && emptycount > 1) {
                    int deleteServers = emptycount - t.getEmptyServers();

                    for (Server s : t.getServers()) {
                        if (s.getPlayerCount() < (0.4 * s.getTemplate().getMax()) && deleteServers > 0) {
                            deleteServers--;

                            //If the minimum server count is not reached after deleting server, delete server.
                            if (t.getServers().size() - 1 >= t.getMin()) {
                                s.delete();
                            }
                        }
                    }
                }
            }

            for (Server s : MasterServer.getInstance().getStaticServerManager().getServers()) {
                if (s.getWrapper() == null && !s.isPreventStart()) {
                    serverWaitList.add(s);
                }
            }

            Iterator<Server> i = serverWaitList.iterator();

            while (i.hasNext()) {
                Server server = i.next();

                if (!server.getInfo().isStaticServer()) {
                    Wrapper bestwrapper = getBestWrapper();

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
                    Wrapper wrapper = MasterServer.getInstance().getWrapper(server.getWrapperUuid());

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
    private Wrapper getBestWrapper() {
        HashMap<Wrapper, Long> wrappers = new HashMap<>();

        for (Wrapper w : MasterServer.getInstance().getWrappers()) {
            if (!w.isBusy()) {
                long difference = w.getRam() - w.getRamInUse();

                //Exclude wrappers which ram is nearly full
                if (difference > 100) {
                    wrappers.put(w, w.getRam() - w.getRamInUse());
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
