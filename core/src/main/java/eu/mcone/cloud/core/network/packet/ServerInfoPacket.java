package eu.mcone.cloud.core.network.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class ServerInfoPacket extends Packet {

    @Getter
    private ServerInfo serverInfo;

    public ServerInfoPacket() {}

    public ServerInfoPacket(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(serverInfo.getUuid().toString());
            out.writeUTF(serverInfo.getName());
            out.writeUTF(serverInfo.getState().toString());
            out.writeUTF(serverInfo.getTemplateName());
            out.writeInt(serverInfo.getId());
            out.writeInt(serverInfo.getRam());
            out.writeInt(serverInfo.getPort());

            byte[] result = stream.toByteArray();
            byteBuf.writeInt(result.length);
            byteBuf.writeBytes(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read(ByteBuf byteBuf) {
        byte[] msg = new byte[byteBuf.readInt()];
        byteBuf.readBytes(msg);

        DataInputStream input = new DataInputStream(new ByteArrayInputStream(msg));
        try {
            UUID uuid = UUID.fromString(input.readUTF());
            String name = input.readUTF();
            ServerState state = ServerState.valueOf(input.readUTF());
            String templateName = input.readUTF();
            int id = input.readInt();
            int ram = input.readInt();
            int port = input.readInt();

            serverInfo = new ServerInfo(uuid, name, templateName, id, ram);
            serverInfo.setState(state);
            serverInfo.setPort(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
