package eu.mcone.cloud.core.api.world;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class CloudWorldManager {

    public static final String WORLD_URL = "/api/world/";
    @Getter
    private static CloudWorldManager instance;

    @Getter
    private final URI storageHost;
    private final JsonParser jsonParser;
    private final MongoCollection<CloudWorld> worldsCollection;
    @Getter
    private final Set<CloudWorld> worlds;

    public CloudWorldManager(MongoDatabase database) {
        this(database, "https://storage.mcone.eu");
    }

    public CloudWorldManager(MongoDatabase database, String storageHost) {
        this(database, storageHost, new JsonParser());
    }

    public CloudWorldManager(MongoDatabase database, String storageHost, JsonParser jsonParser) {
        CloudWorldManager.instance = this;

        try {
            this.worldsCollection = database.getCollection("worlds", CloudWorld.class);
            this.storageHost = new URI(storageHost + WORLD_URL);
            this.jsonParser = jsonParser;
            this.worlds = new HashSet<>();

            reload();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not initialize CloudWorldManager. Storage Host "+storageHost+" is invalid!");
        }
    }

    public void reload() {
        worlds.clear();

        for (CloudWorld world : worldsCollection.find()) {
            this.worlds.add(world);
        }
    }

    public CloudWorld getWorld(String id) {
        for (CloudWorld world : worlds) {
            if (world.getId().equals(id)) {
                return world;
            }
        }

        return null;
    }

    public Set<CloudWorld> getWorlds(String name) {
        Set<CloudWorld> result = new HashSet<>();

        for (CloudWorld world : worlds) {
            if (world.getName().equalsIgnoreCase(name)) {
                result.add(world);
            }
        }

        return result;
    }

    public CloudWorld createWorld(UUID initiator, String name) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();

        JsonObject json = new JsonObject();
        json.addProperty("initiator", initiator.toString());
        json.addProperty("name", name);

        StringEntity requestEntity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);

        HttpPost request = new HttpPost(storageHost.getPath()+"/insert");
        request.setEntity(requestEntity);

        HttpResponse response = client.execute(request);
        HttpEntity entity = request.getEntity();
        int responseCode = response.getStatusLine().getStatusCode();

        if (responseCode == 200) {
            InputStreamReader ir = new InputStreamReader(entity.getContent());
            CloudWorld world = new Gson().fromJson(ir, CloudWorld.class);
            ir.close();

            return world;
        } else return null;
    }

}
