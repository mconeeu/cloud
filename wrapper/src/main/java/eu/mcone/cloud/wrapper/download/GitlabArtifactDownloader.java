/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.download;

import eu.mcone.cloud.core.exception.CloudException;
import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.extern.java.Log;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Artifact;
import org.gitlab4j.api.models.Job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Log
public class GitlabArtifactDownloader {

    private final static File JAR_DIR = new File(WrapperServer.getInstance().getFileManager().getHomeDir().getPath() + File.separator + "jars" + File.separator + "jenkins");
    private GitLabApi gitLabApi;
  
    public GitlabArtifactDownloader() {
        log.fine("Verbinde zum Gitlab Server...");
        gitLabApi = new GitLabApi("https://gitlab.mcone.eu", "pUwScza4PsZoFqPnVsNz");
    }

    public File getArtifact(int projectId, String artifactPath) throws GitLabApiException {
        log.info("Downloading artifact "+artifactPath+" from project with id "+projectId);

        int latestPipeline = gitLabApi.getPipelineApi().getPipelines(projectId).iterator().next().getId();
        List<Job> jobs = gitLabApi.getJobApi().getJobsForPipeline(projectId, latestPipeline, Constants.JobScope.SUCCESS);

        for (Job job : jobs) {
            if (job.getStage().equals("build") && job.getName().equals("build")) {
                Path path = Paths.get(artifactPath);
                System.out.println(path.toString());

                return gitLabApi.getJobApi().downloadSingleArtifactsFile(projectId, job.getId(), path, JAR_DIR);
            }
        }

        return null;
    }

}