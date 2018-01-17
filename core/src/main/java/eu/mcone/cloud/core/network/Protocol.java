package eu.mcone.cloud.core.network;

import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import lombok.Getter;

public enum Protocol {
    SERVER_INFO(1, ServerInfoPacket.class);

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
