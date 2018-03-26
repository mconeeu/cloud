/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network;

import eu.mcone.cloud.core.network.packet.*;
import lombok.Getter;

public enum Protocol {
    MASTER_REQUEST(0, MasterRequestPacketClient.class),
    WRAPPER_REGISTER(1, WrapperRegisterPacketWrapper.class),
    WRAPPER_REGISTER_FROM_STANDALONE(2, WrapperRegisterFromStandalonePacketWrapper.class),
    WRAPPER_SHUTDOWN(3, WrapperShutdownPacketWrapper.class),
    WRAPPER_REQUEST(4, WrapperRequestPacketMaster.class),
    SERVER_INFO(5, ServerInfoPacket.class),
    SERVER_COMMAND_EXECUTE(6, ServerCommandExecutePacketWrapper.class),
    SERVER_CHANGE_STATE(7, ServerChangeStatePacketWrapper.class),
    SERVER_UPDATE_STATE(8, ServerUpdateStatePacket.class),
    SERVER_PLAYER_COUNT_UPDATE(9, ServerPlayerCountUpdatePacketPlugin.class),
    SERVER_REGISTER(10, ServerRegisterPacketPlugin.class),
    SERVER_RESULT(11, ServerResultPacketWrapper.class),
    SERVER_LOG(12, ServerLogPacketClient.class),
    PLUGIN_SERVER_LIST_ADD(13, ServerListPacketAddPlugin.class),
    PLUGIN_SERVER_LIST_REMOVE(14, ServerListPacketRemovePlugin.class),
    CLIENT_RETURN(15, ClientReturnPacketMaster.class);

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
