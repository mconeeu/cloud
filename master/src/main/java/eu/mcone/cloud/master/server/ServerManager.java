/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.cloud.master.wrapper.WrapperManager;
import eu.mcone.cloud.master.MasterServer;

import java.util.*;

public class ServerManager {

    private static HashMap<Server, String> serverWaitList = new HashMap<>();

    //creates or deletes empty servers for or from templates and tries to start servers from serverWaitList
    public ServerManager() {
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                for (HashMap.Entry<String, Template> templateEntry : MasterServer.templates.entrySet()) {
                    Template template = templateEntry.getValue();

                    HashMap<Server, Integer> playercount = new HashMap<>();

                    for (Server server : template.getServers().values()) {
                        //Save actual playercount from every server in an hashmap
                        playercount.put(server, server.getPlayercount());
                    }

                    int emptycount = 0;

                    for (Integer i : playercount.values()) {
                        //Count the amount of servers with 0 players.
                        if (i.equals(0)) {
                            emptycount++;
                        }
                    }

                    //If the amount of empty servers is smaller then the set amount, create more empty servers
                    if (emptycount < template.getEmptyservers()) {
                        //If the maximum server count is not reached after adding server, create server
                        if (template.getServers().size()+1 <= template.getMax()) {
                            template.createServer(1);
                        }
                    //Else if the amount of empty servers is bigger then the set amount, delete empty servers
                    } else if (emptycount > template.getEmptyservers()) {
                        int deleteServers = emptycount - template.getEmptyservers();

                        for (HashMap.Entry<UUID, Server> serverEntry : template.getServers().entrySet()) {
                            if (serverEntry.getValue().getPlayercount()==0 && deleteServers>0) {
                                deleteServers--;

                                //If the minimum server count is not reached after deleting server, delete server.
                                if (template.getServers().size()-1 >= template.getMin()) {
                                    template.deleteServer(serverEntry.getValue().getInfo().getUuid());
                                }
                            }
                        }
                    }
                }

                for (HashMap.Entry<Server, String> serverWrapperEntry : serverWaitList.entrySet()) {
                    Server server = serverWrapperEntry.getKey();
                    String wrapperName = serverWrapperEntry.getValue();

                    if (wrapperName == null) {
                        Wrapper bestwrapper = getBestWrapper();

                        if (bestwrapper != null) {
                            server.setWrapper(bestwrapper);
                            serverWaitList.remove(server);
                            System.out.println("[ServerManager.class] Found wrapper " + bestwrapper.getName() + " for server" + server.getInfo().getName() + "! Creating Server!");
                            bestwrapper.createServer(server);
                        } else {
                            System.out.println("[ServerManager.class] No wrapper for server " + server.getInfo().getName() + " available! Staying in WaitList...");
                        }
                    } else {
                        Wrapper wrapper = WrapperManager.getWrapperbyString(wrapperName);

                        if (wrapper != null) {
                            server.setWrapper(wrapper);
                            serverWaitList.remove(server);
                            System.out.println("[ServerManager.class] Found explicit wrapper " + wrapper.getName() + " for server" + server.getInfo().getName() + "! Creating Server!");
                            wrapper.createServer(server);
                        } else {
                            System.out.println("[ServerManager.class] Explicit wrapper " + wrapperName + " not found for server " + server.getInfo().getName() + "! Staying in WaitList...");
                        }
                    }
                }
            }
        }, 1000, 5000);
    }

    //Returns the best wrapper with less ram
    private static Wrapper getBestWrapper() {
        HashMap<Wrapper, Integer> wrappers = new HashMap<>();

        for (HashMap.Entry<String, Wrapper> entry : MasterServer.wrappers.entrySet()) {
            Wrapper wrapper = entry.getValue();
            int difference = wrapper.getRam() - wrapper.getRamInUse();

            //Exclude wrappers which ram is nearly full
            if (difference > 100) {
                wrappers.put(wrapper, wrapper.getRam() - wrapper.getRamInUse());
            }
        }

        if (wrappers.isEmpty()) {
            return null;
        } else {
            //Return the wrapper with less ram
            return Collections.min(wrappers.entrySet(), HashMap.Entry.comparingByValue()).getKey();
        }
    }

    static void addtoServerWaitList(Server server, String wrapperName) {
        if (serverWaitList.containsKey(server)) {
            System.out.println("[ServerManager.addtoServerWaitList] " + server.getInfo().getName() + " already in ServerWaitList!");
        } else {
            System.out.println("[ServerManager.addtoServerWaitList] Added " + server.getInfo().getName() + " to ServerWaitList!");
            serverWaitList.put(server, wrapperName);
        }
    }

}
