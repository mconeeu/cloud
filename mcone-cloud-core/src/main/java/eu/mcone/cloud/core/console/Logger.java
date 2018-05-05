/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.console;

import java.io.PrintStream;

public class Logger {

    public static void log(String clazz, String msg) {
        log(System.out, "["+clazz+"] "+msg);
    }

    public static void err(String clazz, String msg) {
        log(System.err, "["+clazz+"] "+msg);
    }

    public static void log(Class clazz, String msg) {
        log(System.out, "["+clazz.getSimpleName()+"] "+msg);
    }

    public static void err(Class clazz, String msg) {
        log(System.err, "["+clazz.getSimpleName()+"] "+msg);
    }

    private static void log(final PrintStream stream, final String msg) {
        stream.println(msg+ConsoleColor.RESET);
    }

}
