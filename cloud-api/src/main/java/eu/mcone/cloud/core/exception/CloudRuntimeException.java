/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.exception;

public class CloudRuntimeException extends RuntimeException {

    public CloudRuntimeException() {
        super();
    }

    public CloudRuntimeException(String s) {
        super(s);
    }

    public CloudRuntimeException(Throwable t) {
        super(t);
    }

    public CloudRuntimeException(String s, Throwable t) {
        super(s, t);
    }

}
