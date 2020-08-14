package eu.mcone.cloud.core.packet;

import eu.mcone.networkmanager.api.packet.ClientMessageRequestPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CloudServerForwardedRequestPacket extends ClientMessageRequestPacket {

    private UUID fromServer, toServer;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        super.onWrite(out);
        out.writeUTF(fromServer.toString());
        out.writeUTF(toServer.toString());
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        super.onRead(in);
        fromServer = UUID.fromString(in.readUTF());
        toServer = UUID.fromString(in.readUTF());
    }

}