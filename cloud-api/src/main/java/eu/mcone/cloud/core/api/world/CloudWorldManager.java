package eu.mcone.cloud.core.api.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import eu.mcone.cloud.core.server.CloudWorld;
import group.onegaming.networkmanager.core.api.util.UnZip;
import group.onegaming.networkmanager.core.api.util.Zip;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.Binary;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.mongodb.client.model.Filters.*;

@Log
public class CloudWorldManager {

    public static final String WORLD_CONFIG = "core-config.json";
    private final JsonParser jsonParser;
    private final MongoCollection<CloudWorld> worldCollection;

    public CloudWorldManager(MongoDatabase database) {
        this(new JsonParser(), database);
    }

    public CloudWorldManager(JsonParser jsonParser, MongoDatabase database) {
        this.worldCollection = database.getCollection("worlds", CloudWorld.class);
        this.jsonParser = jsonParser;
    }

    public long countWorlds(String iD) {
        return worldCollection.countDocuments(eq("iD", iD));
    }

    public boolean existsWorld(String iD) {
        return worldCollection.find(eq("iD", iD)).first() != null;
    }

    public FindIterable<CloudWorld> getCloudWorld(String iD) {
        return worldCollection.find(eq("iD", iD));
    }

    public long deleteOne(String iD, int version, long updated) {
        return worldCollection.deleteOne(and(eq("iD", iD), eq("version", version), eq("updated", updated))).getDeletedCount();
    }

    public long deleteAll(String iD) {
        return worldCollection.deleteOne(eq("iD", iD)).getDeletedCount();
    }

    private boolean upload(String creator, JsonElement element, File worldFile, Consumer<Boolean> uploaded) {
        try {
            if (element.getAsJsonObject().has("iD")) {
                if (element.getAsJsonObject().has("configVersion")) {
                    if (element.getAsJsonObject().has("name")) {
                        log.log(Level.INFO, "Starting uploading world...");
                        String id = element.getAsJsonObject().get("iD").getAsString();
                        int configVersion = element.getAsJsonObject().get("configVersion").getAsInt();
                        String name = element.getAsJsonObject().get("name").getAsString();

                        File zipFile = new File(name + ".zip");
                        if (zipFile.exists()) zipFile.delete();
                        new Zip(worldFile, zipFile);
                        FileInputStream fis = new FileInputStream(zipFile);

                        worldCollection.replaceOne(
                                and(
                                        eq("iD", id),
                                        eq("version", configVersion)
                                ), (
                                        new CloudWorld(
                                                id,
                                                name,
                                                configVersion,
                                                creator,
                                                System.currentTimeMillis() / 1000,
                                                IOUtils.toByteArray(fis)
                                        )
                                ),
                                new ReplaceOptions().upsert(true)
                        );

                        fis.close();
                        zipFile.delete();
                        log.log(Level.INFO, "World uploaded, ID " + id);
                        uploaded.accept(true);
                        return true;
                    } else {
                        uploaded.accept(false);
                        throw new JsonIOException("The name element is not contained in the core-config.json file!");
                    }
                } else {
                    uploaded.accept(false);
                    throw new JsonIOException("The configVersion element is not contained in the core-config.json file!");
                }
            } else {
                uploaded.accept(false);
                throw new JsonIOException("The ID element is not contained in the core-config.json file!");
            }
        } catch (IOException e) {
            uploaded.accept(false);
            e.printStackTrace();
        }

        return false;
    }

    public void upload(File worldFolder, Consumer<Boolean> uploaded) {
        upload("SYSTEM", worldFolder, uploaded);
    }

    public void upload(String creator, File worldFolder, Consumer<Boolean> uploaded) {
        boolean succeeded = false;
        if (worldFolder.exists()) {
            for (File file : Objects.requireNonNull(worldFolder.listFiles())) {
                if (!file.isDirectory()) {
                    if (file.getName().equalsIgnoreCase(WORLD_CONFIG)) {
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
                            succeeded = upload(creator, jsonParser.parse(reader), file, uploaded);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            uploaded.accept(succeeded);
        } else {
            log.log(Level.SEVERE, "Error uploading world, worldFolder doesnt exists!");
            uploaded.accept(false);
        }
    }

    public void download(File path, String id, Consumer<CloudWorld> configConsumer) {
        log.log(Level.INFO, "Starting downloading world...");
        if (!path.exists()) {
            path.mkdir();
        }

        CloudWorld world = worldCollection.find(eq("iD", id)).first();
        if (world != null) {
            try {
                world.setWorld(new File(path.getAbsolutePath() + File.separator + world.getName()));

                File zipFile = new File(world.getWorld().getAbsolutePath() + ".zip");
                FileOutputStream fos = new FileOutputStream(zipFile);
                fos.write(world.getBytes());
                fos.close();

                new UnZip(zipFile.getPath(), world.getWorld().getAbsolutePath());
                zipFile.delete();

                world.setConfig(new File(world.getWorld().getAbsolutePath(), WORLD_CONFIG));
                configConsumer.accept(world);
                log.log(Level.INFO, "World successfully uploaded!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            log.log(Level.SEVERE, "Could not download world, the iD " + id + " doesnt exists!");
            configConsumer.accept(null);
        }
    }
}
