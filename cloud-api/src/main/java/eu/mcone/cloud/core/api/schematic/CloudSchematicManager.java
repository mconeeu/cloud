/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api.schematic;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.HashMap;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class CloudSchematicManager {

    public static final String SCHEMATIC_COLLECTION = "schematics";

    @Getter
    private static CloudSchematicManager instance;

    private final MongoCollection<CloudSchematic> schematicCollection;

    @Getter
    private final boolean cache;

    private HashMap<String, CloudSchematic> cachedSchematics;

    public CloudSchematicManager(MongoDatabase mongoDatabase, boolean cache) {
        instance = this;
        schematicCollection = mongoDatabase.getCollection(SCHEMATIC_COLLECTION, CloudSchematic.class);
        this.cache = cache;

        if (cache) {
            cachedSchematics = new HashMap<>();
        }
    }

    /**
     * Checks if the Schematic with the given id exists in the database
     *
     * @param id Schematic id
     * @return boolean
     */
    public boolean existsSchematic(String id) {
        if (cache) {
            if (cachedSchematics.containsKey(id)) {
                return true;
            }
        }

        return schematicCollection.find(eq("_id", id)).first() != null;
    }

    /**
     * Inserts a new Schematic in the database
     *
     * @param schematic CloudSchematic
     * @param override  If a schematic has the same ID, it will be overwritten.
     * @throws KeyAlreadyExistsException If a schematic with the same id already exists in the database.
     */
    public void insertSchematic(CloudSchematic schematic, boolean override) {
        if (override) {
            schematicCollection.replaceOne(eq("_id", schematic.getId()), schematic, new ReplaceOptions().upsert(true));
        } else {
            if (!existsSchematic(schematic.getId())) {
                schematicCollection.insertOne(schematic);
            } else {
                throw new KeyAlreadyExistsException("The id " + schematic.getId() + " already exists in the database!");
            }
        }

        if (cache) {
            cachedSchematics.put(schematic.getId(), schematic);
        }
    }

    /**
     * Inserts a new Schematic in the database
     *
     * @param id       Schematic id
     * @param author   The one who created the schematic
     * @param path     The path to the schematic file
     * @param override If a schematic has the same ID, it will be overwritten.
     * @throws KeyAlreadyExistsException If a schematic with the same id already exists in the database.
     */
    public void insertSchematic(String id, UUID author, String path, boolean override) {
        CloudSchematic cloudSchematic = new CloudSchematic(id, author);
        cloudSchematic.upload(path, override);
    }

    /**
     * Returns the Schematic for the given id
     *
     * @param id Schematic id
     * @return CloudSchematic
     * @throws NullPointerException If the Schematic with the given id doesn't exists in the database
     */
    public CloudSchematic getSchematic(String id) {
        if (cache) {
            if (cachedSchematics.containsKey(id)) {
                return cachedSchematics.get(id);
            }
        }

        CloudSchematic cloudSchematic = schematicCollection.find(eq("_id", id)).first();

        if (cloudSchematic != null) {
            if (cache) {
                cachedSchematics.put(id, cloudSchematic);
            }

            return cloudSchematic;
        } else {
            throw new NullPointerException("Could not find schematic with the id " + id);
        }
    }

    /**
     * Deletes a Schematic for the given id
     *
     * @param id Schematic id
     */
    public void deleteSchematic(String id) {
        if (cache) {
            cachedSchematics.remove(id);
        }

        schematicCollection.deleteOne(eq("_id", id));
    }

    /**
     * Counts all available Schematics in the database
     *
     * @return long
     */
    public long countSchematics() {
        return schematicCollection.countDocuments();
    }
}
