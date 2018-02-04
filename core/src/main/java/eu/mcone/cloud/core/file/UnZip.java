/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.file;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {

    public UnZip(String file, String dest) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry z;

        Enumeration<? extends ZipEntry> entryEnumeration = zipFile.entries();
        while (entryEnumeration.hasMoreElements()) {
            z = entryEnumeration.nextElement();
            extractEntry(zipFile, z, dest);
        }
    }

    private void extractEntry(ZipFile zipFile, ZipEntry entry, String destDir) throws IOException {
        final byte[] buffer = new byte[0xFFFF];
        File file = new File(destDir, entry.getName());

        if (entry.isDirectory())
            file.mkdirs();
        else {
            new File(file.getParent()).mkdirs();

            InputStream is = null;
            OutputStream os = null;

            try {
                is = zipFile.getInputStream(entry);
                os = new FileOutputStream(file);

                int len;
                while ((len = is.read(buffer)) != -1) os.write(buffer, 0, len);
            } finally {
                if (os != null) os.close();
                if (is != null) is.close();
            }
        }
    }

}
