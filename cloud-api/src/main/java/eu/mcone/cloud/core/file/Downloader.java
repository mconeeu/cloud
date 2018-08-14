/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.file;

import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;

@Log
public class Downloader {

    public static void download(String url, File output) throws IOException {
        Calendar outdated = Calendar.getInstance();
        outdated.add(Calendar.MONTH, -1);

        if (!url.equals("") && (!output.exists() || new Date(output.lastModified()).before(outdated.getTime()))) {
            log.info("Downloading "+output.getName());

            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            Files.copy(inputStream, output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
