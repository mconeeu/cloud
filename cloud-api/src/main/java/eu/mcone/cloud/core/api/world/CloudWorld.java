/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api.world;

import com.google.gson.Gson;
import group.onegaming.networkmanager.core.api.util.UnZip;
import group.onegaming.networkmanager.core.api.util.Zip;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

@Getter
public class CloudWorld {

    private final String id;
    @Setter
    private String name;
    private final List<String> contributors;
    private final List<WorldVersion> versions;

    @BsonCreator
    public CloudWorld(
            @BsonProperty("_id") String id,
            @BsonProperty("name") String name,
            @BsonProperty("contributors") List<String> contributors,
            @BsonProperty("versions") List<WorldVersion> versions
    ) {
        this.id = id;
        this.name = name;
        this.contributors = contributors;
        this.versions = versions;
    }

    public boolean download(File worldFile) throws IOException {
        return download(worldFile, getLatestVersion().getVersion());
    }

    /**
     * downloads the world where the given id
     *
     * @param version   specific version
     * @param worldFile target location where the world should be downloaded
     */
    public boolean download(File worldFile, int[] version) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();

        try {
            // Create GET request
            URIBuilder builder = new URIBuilder(CloudWorldManager.getInstance().getStorageHost());
            builder.setParameter("id", id).setParameter("version", Arrays.toString(version));

            HttpGet request = new HttpGet(builder.build());

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == 200) {
                InputStream inputStream = entity.getContent();

                File output = new File(worldFile.getParentFile(), "." + name + "-temp.zip");
                if (output.exists()) {
                    output.delete();
                }
                FileOutputStream fos = new FileOutputStream(output);
                int byte_;
                while ((byte_ = inputStream.read()) != -1) {
                    fos.write(byte_);
                }

                inputStream.close();
                fos.close();

                new UnZip(output.getAbsolutePath(), worldFile.getAbsolutePath());
                output.deleteOnExit();

                return true;
            } else return false;
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not download World " + name + ". Error in QueryString Parameter!", e);
        }
    }

    public boolean commit(WorldVersionType type, File worldFile, UUID author, String changelog) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();

        File zip = new File(worldFile.getParent(), "." + name + "-temp.zip");
        if (zip.exists()) {
            zip.delete();
        }
        new Zip(worldFile, zip);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("document", zip);
        builder.addTextBody("increase", type.name().toLowerCase());
        builder.addTextBody("author", author.toString());
        builder.addTextBody("changelog", changelog);

        HttpPost request = new HttpPost(CloudWorldManager.getInstance().getStorageHost().getPath() + "/" + id);
        request.setEntity(builder.build());

        HttpResponse response = client.execute(request);
        HttpEntity entity = request.getEntity();
        int responseCode = response.getStatusLine().getStatusCode();

        if (responseCode == 200) {
            InputStreamReader ir = new InputStreamReader(entity.getContent());
            CloudWorld world = new Gson().fromJson(ir, CloudWorld.class);
            ir.close();

            WorldVersion newVersion = world.getVersions().iterator().next();
            versions.add(newVersion);
            return true;
        } else return false;
    }

    public WorldVersion getVersion(int[] version) {
        for (WorldVersion worldVersion : this.versions) {
            if (worldVersion.equals(version)) {
                return worldVersion;
            }
        }

        return null;
    }

    public WorldVersion getLatestVersion() {
        Collections.sort(this.versions);
        return this.versions.get(this.versions.size() - 1);
    }

    @Override
    public String toString() {
        return "CloudWorld{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", contributors=" + contributors +
                ", versions=" + versions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudWorld cloudWorld = (CloudWorld) o;
        return id.equals(cloudWorld.id) &&
                name.equals(cloudWorld.name) &&
                contributors.equals(cloudWorld.contributors) &&
                versions.equals(cloudWorld.versions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, contributors, versions);
    }

}
