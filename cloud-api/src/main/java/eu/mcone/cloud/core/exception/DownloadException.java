/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.exception;

public class DownloadException extends CloudException {

    public DownloadException() {
        super();
    }

    public DownloadException(String s) {
        super(s);
    }

    public DownloadException(Throwable t) {
        super(t);
    }

    public DownloadException(String s, Throwable t) {
        super(s, t);
    }

}
