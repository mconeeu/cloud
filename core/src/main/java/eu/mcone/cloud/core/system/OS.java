/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.system;

import lombok.Getter;

public class OS {

    @Getter
    private String os_name;

    public enum OS_Type {
        WINDOWS,
        LINUX,
        SOLARIS,
        MAC,
        UNKNOWN,
    };


    public OS() {
        final String OS = System.getProperty("os.name").toLowerCase();

        if(OS.contains("LINUX") || OS.contains("linux") || OS.contains("unix") || OS.contains("nix") || OS.contains("aix")){
             this.os_name = OS_Type.LINUX.toString();
             System.out.println("[OS.class] This system runs on a " + this.os_name + " system");
        }

        if(OS.contains("MAC") || OS.contains( "mac")){
            this.os_name = OS_Type.MAC.toString();
            System.out.println("[OS.class] This system runs on a " + this.os_name + " system");
        }


        if(OS.contains("SOLARIS") || OS.contains("solaris") || OS.contains("SUNOS") || OS.contains("sunos")){
            this.os_name = OS_Type.SOLARIS.toString();
            System.out.println("[OS.class] This system runs on a " + this.os_name + " system");
        }

        String[] trimmed_os = OS.split(" ");
        if(trimmed_os[0].contains("WINDOWS") || trimmed_os[0].contains("windows")){
            this.os_name = OS_Type.WINDOWS.toString();
            System.out.println("[OS.class] This system runs on a " + this.os_name + " system");
        }else{
            System.out.println("[OS.class] This system runs on a " + this.os_name + " system");
        }
    }
}
