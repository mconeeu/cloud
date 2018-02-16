/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.cloud.master.wrapper.WrapperManager;
import eu.mcone.cloud.master.MasterServer;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerManager {

    @Getter
    private Wrapper wrapper;

    private ScheduledExecutorService es;
    private static List<Server> serverWaitList = new ArrayList<>();

    //creates or deletes empty servers for or from templates and tries to start servers from serverWaitList
    public ServerManager() {
        es = Executors.newSingleThreadScheduledExecutor();
        es.scheduleAtFixedRate(() -> {
            for (Template t : MasterServer.getInstance().getTemplates()) {
                HashMap<Server, Integer> playercount = new HashMap<>();

                for (Server s : t.getServers()) {
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
                                t.deleteServer(s);
                            }
                        }
                    }
                }
            }

            Iterator<Server> i = serverWaitList.iterator();

            while (i.hasNext()) {
                Server server = i.next();
                String wrapperName = server.getWrapperName();

                if (wrapperName == null) {
                    Wrapper bestwrapper = getBestWrapper();
                    this.wrapper = bestwrapper;

                    if (bestwrapper != null) {
                        if(bestwrapper.isProgressing()){
                            System.out.println("[ServerManager.class] Server '" + server.getInfo().getName() + "' waiting...");
                        }else{
                            server.setWrapper(bestwrapper);
                            i.remove();
                            System.out.println("[ServerManager.class] Found wrapper " + bestwrapper.getName() + " for server" + server.getInfo().getName() + "! Creating Server!");
                            bestwrapper.createServer(server);
                            server.start();
                            break;
                        }
                    } else {
                        System.out.println("[ServerManager.class] No wrapper for server " + server.getInfo().getName() + " available! Staying in WaitList...");
                    }
                } else {
                    Wrapper wrapper = WrapperManager.getWrapperbyString(wrapperName);

                    if (wrapper != null) {
                        if(wrapper.isProgressing()){
                            System.out.println("[ServerManager.class] Server '" + server.getInfo().getName() + "' waiting...");
                        }else{
                            server.setWrapper(wrapper);
                            i.remove();
                            System.out.println("[ServerManager.class] Found explicit wrapper " + wrapper.getName() + " for server" + server.getInfo().getName() + "! Creating Server!");
                            wrapper.createServer(server);
                            server.start();
                            break;
                        }
                    } else {
                        System.out.println("[ServerManager.class] Explicit wrapper " + wrapperName + " not found for server " + server.getInfo().getName() + "! Staying in WaitList...");
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    //Returns the best wrapper with less ram
    private Wrapper getBestWrapper() {
        HashMap<Wrapper, Long> wrappers = new HashMap<>();

        for (Wrapper w : MasterServer.getInstance().getWrappers()) {
            long difference = w.getRam() - w.getRamInUse();

            //Exclude wrappers which ram is nearly full
            if (difference > 100) {
                wrappers.put(w, w.getRam() - w.getRamInUse());
            }
        }

        if (wrappers.isEmpty()) {
            return null;
        } else {
            //Return the wrapper with less ram
            return Collections.min(wrappers.entrySet(), HashMap.Entry.comparingByValue()).getKey();
        }
    }

    void addtoServerWaitList(Server server) {
        if (serverWaitList.contains(server)) {
            System.out.println("[ServerManager.addtoServerWaitList] " + server.getInfo().getName() + " already in ServerWaitList!");
        } else {
            System.out.println("[ServerManager.addtoServerWaitList] Added " + server.getInfo().getName() + " to ServerWaitList!");
            serverWaitList.add(server);
        }
    }

    public void shutdown() {
        es.shutdown();
    }

}
