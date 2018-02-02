/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.util;

import eu.mcone.cloud.wrapper.directorymanager.DirectoryCreator;

import java.io.File;

public class Var {

    public static String getSystemPath(){
        try{
            File path = new File(DirectoryCreator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
                    .replaceFirst("/", "")
                    .replace("wrapper.jar", ""));

            return path.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
