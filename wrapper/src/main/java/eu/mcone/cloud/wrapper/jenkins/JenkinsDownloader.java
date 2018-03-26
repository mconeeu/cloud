/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Artifact;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;
import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.util.Map;

public class JenkinsDownloader {

    public enum CiServer {
        MCONE("http://78.46.249.205:8080", "cloudsystem", "UZuV0qgQuIxsgp3W");

        @Getter
        private String uri, user, password;

        CiServer(String uri, String user, String password) {
            this.uri = uri;
            this.user = user;
            this.password = password;
        }
    }

    private String jarPath;
    private JenkinsServer jenkinsServer;

    public JenkinsDownloader(CiServer server) {
        this.jarPath = WrapperServer.getInstance().getFileManager().getHomeDir().getPath() + File.separator + "jars" + File.separator + "jenkins";

        try {
            jenkinsServer = new JenkinsServer(new URI(server.getUri()), server.getUser(), server.getPassword());
        } catch (URISyntaxException e) {
            Logger.err(getClass(), "Der JenkinsServer "+server.toString()+" ist nicht erreichbar:");
            Logger.err(getClass(), e.getMessage());
        }
    }

    public File getJenkinsArtifact(String jobName, String artifactName) {
        String[] nameparts = artifactName.split("-");

        try {
            Map<String, Job> jobs = jenkinsServer.getJobs();
            BuildWithDetails build = jobs.get(jobName).details().getLastSuccessfulBuild().details();

            for (Artifact artifact : build.getArtifacts()) {
                String[] parts = artifact.getFileName().split("-");
                boolean equal = true;

                for (int i = 0; i < nameparts.length; i++) {
                    if (nameparts[i].equals(parts[i])) {
                        equal = true;
                    } else {
                        equal = false;
                        break;
                    }
                }

                if (equal) {
                    File jar = new File(jarPath + File.separator + artifact.getFileName());

                    if (!jar.exists()) {
                        String oldBuild = WrapperServer.getInstance().getConfig().getConfig().getSection("jenkins").getString(jobName+":"+artifactName);

                        if (!build.getId().equals(oldBuild)) {
                            jar.delete();

                            FileOutputStream fos = new FileOutputStream(jar);
                            Logger.log("JenkinsDownloader", "Downloading job " + jobName + " to " + jar.getPath() + "...");
                            fos.getChannel().transferFrom(Channels.newChannel(build.downloadArtifact(artifact)), 0, Long.MAX_VALUE);

                            WrapperServer.getInstance().getConfig().getConfig().getSection("jenkins").set(jobName+":"+artifactName, build.getId());
                        }
                    }

                    return jar;
                }
            }
        } catch (IOException | URISyntaxException e) {
            Logger.err(getClass(), "Fehler beim herunterladen von "+jobName+":"+artifactName+":");
            e.printStackTrace();
        }

        return null;
    }

}