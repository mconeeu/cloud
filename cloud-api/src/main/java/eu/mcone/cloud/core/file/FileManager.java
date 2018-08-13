/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.file;

import lombok.Getter;

import java.io.File;

public class FileManager {

    @Getter
    private File homeDir;

    public FileManager() {
        homeDir = new File(System.getProperty("user.dir"));
    }

    public void createHomeDir(String path) {
        String s = File.separator;
        File file = new File(homeDir+s+path);

        if (!file.exists()) {
            file.mkdir();
            System.out.println("creating "+file.getPath());
        }
    }

}