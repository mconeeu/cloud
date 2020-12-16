/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api.schematic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CloudSchematic {

    private String id;
    private UUID author;
    private byte[] schematicData;
    private long created;

    public CloudSchematic(String id, UUID author) {
        this.id = id;
        this.author = author;
        this.created = System.currentTimeMillis() / 1000;
    }

    /**
     * Fetches the content from the Schematic file and creates a database entry.
     *
     * @param path     The path of the schematic file to be transferred
     * @param override If a schematic has the same ID, it will be overwritten.
     * @throws NullPointerException      If the file doesn't exists.
     * @throws KeyAlreadyExistsException If a schematic with the same id already exists in the database.
     * @throws InstantiationException    If the CloudSchematicManager isn't instantiated.
     */
    public void upload(String path, boolean override) {
        try {
            if (CloudSchematicManager.getInstance() != null) {
                if (!CloudSchematicManager.getInstance().existsSchematic(id)) {
                    File file = new File(path);

                    if (file.exists()) {
                        this.schematicData = FileUtils.readFileToByteArray(file);
                        CloudSchematicManager.getInstance().insertSchematic(this, override);
                    } else {
                        throw new NullPointerException("Could not find file " + path);
                    }
                } else {
                    throw new KeyAlreadyExistsException("The id " + id + " already exists in the database!");
                }
            } else {
                throw new InstantiationException("No instance of the Cloud schematic manager has been created yet!");
            }
        } catch (InstantiationException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the schematic file
     *
     * @param path     The path where the schematic gets saved
     * @param override If the schematic already exists, it can be overwritten with this option.
     * @throws InstantiationException If the CloudSchematicManager isn't instantiated.
     */
    public void create(String path, boolean override) {
        try {
            if (CloudSchematicManager.getInstance() != null) {
                File file = new File(path, id);

                if (file.exists()) {
                    if (override) {
                        FileUtils.writeByteArrayToFile(file, schematicData);
                    }
                } else {
                    FileUtils.writeByteArrayToFile(file, schematicData);
                }
            } else {
                throw new InstantiationException("No instance of the Cloud schematic manager has been created yet!");
            }
        } catch (InstantiationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "CloudSchematic{" +
                "id='" + id + '\'' +
                ", schematic=" + Arrays.toString(schematicData) +
                ", author=" + author +
                ", created=" + created +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudSchematic that = (CloudSchematic) o;
        return created == that.created &&
                Objects.equals(id, that.id) &&
                Arrays.equals(schematicData, that.schematicData) &&
                Objects.equals(author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, created, author, schematicData);
    }
}
