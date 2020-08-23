package eu.mcone.cloud.core.packet;

import eu.mcone.cloud.core.api.CloudMaster;
import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.api.server.SimpleServer;
import eu.mcone.cloud.core.api.template.Template;
import eu.mcone.cloud.core.api.wrapper.Wrapper;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import group.onegaming.networkmanager.api.packet.ClientMessageResponsePacket;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

@NoArgsConstructor
@Getter
public class CloudInfoResponsePacket extends ClientMessageResponsePacket {

    private Collection<SimpleServer> servers;
    private Collection<Template> templates;

    public CloudInfoResponsePacket(String responseUuid, CloudMaster cloudMaster) {
        super(responseUuid, HttpResponseStatus.OK);

        this.servers = new ArrayList<>(cloudMaster.getServers());
        this.templates = cloudMaster.getTemplates();
    }

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        out.writeInt(templates.size());
        for (Template t : templates) {
            out.writeUTF(t.getName());
            out.writeInt(t.getMaxPlayers());
            out.writeInt(t.getMinServers());
            out.writeInt(t.getMaxServers());
            out.writeInt(t.getEmptyServers());
            out.writeLong(t.getRam());
            out.writeUTF(t.getVersion().toString());
        }

        out.writeInt(servers.size());
        for (SimpleServer s : servers) {
            ServerInfoPacket.writeServerInfo(out, s.getInfo());
            out.writeUTF(s.getTemplate().getName());

            out.writeInt(s.getPlayers().size());
            for (Map.Entry<UUID, String> e : s.getPlayers().entrySet()) {
                out.writeUTF(e.getKey().toString());
                out.writeUTF(e.getValue());
            }

            out.writeUTF(s.getState().toString());
        }
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        this.templates = new ArrayList<>();
        int tSize = in.readInt();
        for (int i = 0; i < tSize; i++) {
            this.templates.add(new PacketTemplate(
                    in.readUTF(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readLong(), ServerVersion.valueOf(in.readUTF())
            ));
        }

        this.servers = new ArrayList<>();
        int sSize= in.readInt();
        for (int i = 0; i < sSize; i++) {
            ServerInfo info = ServerInfoPacket.readServerInfo(in);

            String templateName = in.readUTF();
            Template template = null;
            for (Template t : templates) {
                if (t.getName().equals(templateName)) {
                    template = t;
                }
            }

            Map<UUID, String> players = new HashMap<>();
            int pSize = in.readInt();
            for (int x = 0; x < pSize; x++) {
                players.put(
                        UUID.fromString(in.readUTF()),
                        in.readUTF()
                );
            }

            ServerState state = ServerState.valueOf(in.readUTF());

            this.servers.add(new PacketServer(
                    info, template, players, state
            ));
        }
    }

    @RequiredArgsConstructor
    @Getter
    public class PacketTemplate implements Template {
        @Setter private Set<Server> servers;
        private final String name;
        private final int maxPlayers;
        private final int minServers;
        private final int maxServers;
        private final int emptyServers;
        private final long ram;
        private final ServerVersion version;
    }

    @RequiredArgsConstructor
    @Getter
    public class PacketServer implements SimpleServer {
        private final ServerInfo info;
        private final Template template;
        private final Wrapper wrapper = null;
        private final Map<UUID, String> players;
        private final ServerState state;

        @Override
        public int getPlayerCount() {
            return players.size();
        }
    }

}
