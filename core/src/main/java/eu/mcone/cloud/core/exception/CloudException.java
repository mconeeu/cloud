/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.exception;

public class CloudException extends Exception {

    public CloudException() {
        super();
    }

    public CloudException(String s) {
        super(s);
    }

    public CloudException(Throwable t) {
        super(t);
    }

    public CloudException(String s, Throwable t) {
        super(s, t);
    }

}
