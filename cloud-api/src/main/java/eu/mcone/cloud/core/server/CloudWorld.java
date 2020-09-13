/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.server;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.io.File;

@Getter
public class CloudWorld {

    private final String name, id;
    private final int version;
    private final String creator;
    private final long updated;
    private final byte[] bytes;

    @BsonIgnore
    @Setter
    private transient File world;
    @BsonIgnore
    @Setter
    private transient File config;

    public CloudWorld(String name, String id, int version, String creator, long updated, byte[] bytes) {
        this.name = name;
        this.id = id;
        this.version = version;
        this.creator = creator;
        this.updated = updated;
        this.bytes = bytes;
    }
}
