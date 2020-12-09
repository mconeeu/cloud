package eu.mcone.cloud.core.api.world;

import lombok.Getter;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Getter
public class WorldVersion implements Comparable<WorldVersion> {

    private final int[] version;
    private final String author, changelog;
    private final long time;

    @BsonCreator
    public WorldVersion(
            @BsonProperty("version") int[] version,
            @BsonProperty("author") String author,
            @BsonProperty("changelog") String changelog,
            @BsonProperty("time") long time) {
        this.version = version;
        this.author = author;
        this.changelog = changelog;
        this.time = time;
    }

    public boolean equals(int[] version) {
        for (int i = 0; i < version.length; i++) {
            int ownVersion = i < this.version.length ? this.version[i] : 0;

            if (version[i] != ownVersion) {
                return false;
            }
        }

        return true;
    }

    public int compareTo(int[] version) {
        for (int i = 0; i < version.length; i++) {
            int ownVersion = i < this.version.length ? this.version[i] : 0;

            if (version[i] > ownVersion) {
                return 1;
            } else if (i == version.length-1) {
                return version[i] == ownVersion ? 0 : -1;
            }
        }

        return Math.abs(version.length - this.version.length);
    }

    @Override
    public int compareTo(WorldVersion version) {
        return compareTo(version.version);
    }

}
