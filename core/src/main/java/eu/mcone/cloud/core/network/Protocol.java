/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network;

import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.core.server.ServerInfo;
import lombok.Getter;

public enum Protocol {
    WRAPPER_REGISTER(0, WrapperRegisterPacket.class),
    SERVER_INFO(1, ServerInfoPacket.class),
    SERVER_COMMAND_EXECUTE(2, ServerCommandExecutePacket.class),
    SERVER_CHANGE_STATE(3, ServerChangeStatePacket.class),
    SERVER_RESULT(6, ServerResultPacket.class);

    @Getter
    private int id;
    @Getter
    private Class<? extends Packet> clazz;

    Protocol(int id, Class<? extends Packet> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public static int getIDByClass(Class<? extends Packet> clazz) {
        for (Protocol protocol : values()) {
            if (protocol.getClazz().equals(clazz)) {
                return protocol.getId();
            }
        }

        return -1;
    }

    public static Class<? extends Packet> getClassbyID(int id) {
        for (Protocol protocol : values()) {
            if (protocol.getId() == id) {
                return protocol.getClazz();
            }
        }

        return null;
    }
}