/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import eu.mcone.cloud.core.console.Logger;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

    public static void downloadFromJenkins(CiServer server, String jobName, File target) throws URISyntaxException, IOException {
        JenkinsServer jenkins = new JenkinsServer(new URI(server.getUri()), server.getUser(), server.getPassword());
        Map<String, Job> jobs = jenkins.getJobs();

        JobWithDetails job = jobs.get(jobName).details();
        URL file = new URL(job.getLastSuccessfulBuild().getUrl());

        FileOutputStream fos = new FileOutputStream(target);
        Logger.log("JenkinsDownloader", "Downloading job "+jobName+" to "+target.getPath()+"...");
        fos.getChannel().transferFrom(Channels.newChannel(file.openStream()), 0, Long.MAX_VALUE);
    }

}