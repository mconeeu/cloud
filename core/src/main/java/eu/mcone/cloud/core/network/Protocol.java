/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network;

import eu.mcone.cloud.core.network.packet.*;
import lombok.Getter;

public enum Protocol {
    WRAPPER_REGISTER(0, WrapperRegisterPacketWrapper.class),
    Wrapper_REGISTER_FROM_STANDALONE(1, WrapperRegisterFromStandalonePacketWrapper.class),
    WRAPPER_SHUTDOWN(2, WrapperShutdownPacketWrapper.class),
    SERVER_INFO(3, ServerInfoPacket.class),
    SERVER_COMMAND_EXECUTE(4, ServerCommandExecutePacketWrapper.class),
    SERVER_CHANGE_STATE(5, ServerChangeStatePacketWrapper.class),
    SERVER_UPDATE_STATE(6, ServerUpdateStatePacketWrapper.class),
    SERVER_PLAYER_COUNT_UPDATE(7, ServerPlayerCountUpdatePacketPlugin.class),
    SERVER_REGISTER(8, ServerRegisterPacketPlugin.class),
    SERVER_RESULT(9, ServerResultPacketWrapper.class),
    PLUGIN_SERVER_LIST_ADD(10, ServerListPacketAddPlugin.class),
    PLUGIN_SERVER_LIST_REMOVE(11, ServerListPacketRemovePlugin.class);

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
