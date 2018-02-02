/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.directorymanager;

import java.io.File;

public class DirectoryCreator {

    @lombok.Getter
    private String home_directory;

    @lombok.Getter
    private String server_directory;

    @lombok.Getter
    private String template_directory;

    @lombok.Getter
    private String config_directory;

    public DirectoryCreator() {
        try {
            File path = new File(DirectoryCreator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
                    .replaceFirst("/", "")
                    .replace("wrapper.jar", ""));

            File home_directory = new File(path + "\\wrapper");
            File template_directory = new File(path + "\\wrapper\\templates");
            File server_directory = new File(path + "\\wrapper\\servers");
            File config_directory = new File(path + "\\wrapper\\config");

            this.home_directory = home_directory.toString();
            this.server_directory = server_directory.toString();
            this.template_directory = template_directory.toString();
            this.config_directory = config_directory.toString();

            if (!(home_directory.exists())) {
                System.out.println("Create Home directory Path:" + home_directory);
                home_directory.mkdir();
            }

            if (!(template_directory.exists())) {
                System.out.println("Create Template directory Path: " + template_directory);
                template_directory.mkdir();
            }

            if (!(server_directory.exists())) {
                System.out.println("Create server directory Path: " + server_directory);
                server_directory.mkdir();
            }

            if (!(config_directory.exists())) {
                System.out.println("Create config directory Path: " + config_directory);
                config_directory.mkdir();
            }

            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
