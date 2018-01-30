/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.directorymanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CopyManager {

    public static void copyFilesInDirectory(File from, File to) throws IOException {

        if(to.exists()){
            return;
        }else{
            to.mkdirs();
        }

        for (File file : from.listFiles()) {
            if (file.isDirectory()) {
                copyFilesInDirectory(file, new File(to.getAbsolutePath() + "/" + file.getName()));
            } else {
                File n = new File(to.getAbsolutePath() + "/" + file.getName());
                Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return;
    }

    public static void copyFileToDirectory(File file, File to) throws IOException {
        if(!to.exists()) {
            to.mkdirs();
        }

        File n = new File(to.getAbsolutePath() + "/" + file.getName());
        Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return;
    }
}
