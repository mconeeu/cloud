/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network;

import eu.mcone.cloud.core.network.packet.*;
import lombok.Getter;

public enum Protocol {
    WRAPPER_REGISTER(0, WrapperRegisterPacketWrapper.class),
    WRAPPER_SHUTDOWN(1, WrapperShutdownPacketWrapper.class),
    SERVER_INFO(2, ServerInfoPacket.class),
    SERVER_COMMAND_EXECUTE(3, ServerCommandExecutePacketWrapper.class),
    SERVER_CHANGE_STATE(4, ServerChangeStatePacketWrapper.class),
    SERVER_UPDATE_STATE(5, ServerUpdateStatePacketWrapper.class),
    SERVER_PLAYER_COUNT_UPDATE(6, ServerPlayerCountUpdatePacketPlugin.class),
    SERVER_RESULT(7, ServerResultPacketWrapper.class),
    PLUGIN_SERVER_LIST(8, ServerListPacketAddPlugin.class);

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
