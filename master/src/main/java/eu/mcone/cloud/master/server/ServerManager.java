/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
                    if (s.getWrapper() == null) {
                        serverWaitList.add(s);
                    }

                    //Save actual playercount from every server in an hashmap
                    if (s.getPlayerCount() >= 0) {
                        playercount.put(s, s.getPlayerCount());
                    }
                }

                int emptycount = 0;

                for (Integer i : playercount.values()) {
                    //Count the amount of servers with 0 players.
                    if (i.equals(0)) {
                        emptycount++;
                    }
                }

                //If the amount of empty servers is smaller then the set amount, create more empty servers
                if (emptycount < t.getEmptyservers()) {
                    //If the maximum server count is not reached after adding server, create server
                    if (t.getServers().size()+1 <= t.getMax()) {
                        t.createServer(1);
                    }
                    //Else if the amount of empty servers is bigger then the set amount, delete empty servers
                } else if (emptycount > t.getEmptyservers()) {
                    int deleteServers = emptycount - t.getEmptyservers();

                    for (Server s : t.getServers()) {
                        if (s.getPlayerCount()==0 && deleteServers>0) {
                            deleteServers--;

                            //If the minimum server count is not reached after deleting server, delete server.
                            if (t.getServers().size()-1 >= t.getMin()) {
                                s.delete();
                            }
                        }
                    }
                }
            }

            for (Server s : MasterServer.getInstance().getStaticServerManager().getServers()) {
                if (s.getWrapper() == null) {
                    serverWaitList.add(s);
                }
            }

            Iterator<Server> i = serverWaitList.iterator();

            while (i.hasNext()) {
                Server server = i.next();
                UUID wrapperUuid = server.getWrapperUuid();

                if (wrapperUuid == null) {
                    Wrapper bestwrapper = getBestWrapper();

                    if (bestwrapper != null) {
                        server.setWrapper(bestwrapper);
                        i.remove();
                        Logger.log(getClass(), "Found wrapper " + bestwrapper.getUuid() + " for server " + server.getInfo().getName() + "! Creating Server!");
                        bestwrapper.createServer(server);
                        server.start();
                    } else {
                        Logger.log(getClass(), "No wrapper for server " + server.getInfo().getName() + " available! Staying in WaitList...");
                    }
                } else {
                    Wrapper wrapper = MasterServer.getInstance().getWrapper(wrapperUuid);

                    if (wrapper != null && !wrapper.isBusy()) {
                        server.setWrapper(wrapper);
                        i.remove();
                        Logger.log(getClass(), "Found explicit wrapper " + wrapper.getUuid() + " for server " + server.getInfo().getName() + "! Creating Server!");
                        wrapper.createServer(server);
                        server.start();
                        break;
                    } else {
                        Logger.log(getClass(), "Explicit wrapper " + wrapperUuid + " not found for server " + server.getInfo().getName() + "! Staying in WaitList...");
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
