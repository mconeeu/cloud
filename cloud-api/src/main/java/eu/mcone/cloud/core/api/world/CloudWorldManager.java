package eu.mcone.cloud.core.api.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import group.onegaming.networkmanager.core.api.util.UnZip;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

public class CloudWorldManager {

    public static final String WORLD_URL = "/api/world/";

    @Getter
    private URI requestUri;
    @Getter
    private URI apiUri;
    @Getter
    private JsonParser jsonParser;

    public CloudWorldManager(String requestUri) {
        try {
            this.requestUri = new URI(requestUri);
            this.apiUri = new URI(requestUri + WORLD_URL);
            this.jsonParser = new JsonParser();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public CloudWorldManager(String requestUri, JsonParser jsonParser) {
        try {
            this.requestUri = new URI(requestUri);
            this.apiUri = new URI(requestUri + WORLD_URL);
            this.jsonParser = jsonParser;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * downloads the world where the given id
     *
     * @param id          world id
     * @param version     specific version
     * @param destination where the file gets saved
     * @param name        name of the file
     * @param consumer    consumer indicating whether the download was successful
     */
    public void download(String id, int[] version, String destination, String name, Consumer<Boolean> consumer) {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            // Create GET request
            HttpGet request = new HttpGet(apiUri);
            // add values to header
            request.addHeader("id", id);
            request.addHeader("version", Arrays.toString(version));

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == 200) {
                InputStream inputStream = entity.getContent();

                File output = new File(destination + name + ".zip");
                FileOutputStream fos = new FileOutputStream(output);
                int byte_;
                while ((byte_ = inputStream.read()) != -1) {
                    fos.write(byte_);
                }

                inputStream.close();
                fos.close();

                new UnZip(output.getAbsolutePath(), destination);
                output.deleteOnExit();

                consumer.accept(true);
            } else {
                consumer.accept(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns a world where the given id
     *
     * @param id world id
     * @return JsonObject
     */
    public JsonObject getWorld(String id) {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();

            // Create GET request
            HttpGet request = new HttpGet(apiUri + id);
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == 200) {
                final String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                return jsonParser.parse(json).getAsJsonObject();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * returns all in the database stored worlds
     *
     * @return JsonArray
     */
    public JsonArray getWorlds() {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            // Create GET request
            HttpGet request = new HttpGet(apiUri + "/worlds");

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == 200) {
                final String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                return jsonParser.parse(json).getAsJsonArray();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
