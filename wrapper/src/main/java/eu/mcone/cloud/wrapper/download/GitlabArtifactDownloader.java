/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.download;

import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.extern.java.Log;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Job;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Log
public class GitlabArtifactDownloader {

    private final static File JAR_DIR = new File(WrapperServer.getInstance().getFileManager().getHomeDir().getPath() + File.separator + "jars" + File.separator + "gitlab");
    private GitLabApi gitLabApi;
  
    public GitlabArtifactDownloader() {
        gitLabApi = new GitLabApi("https://gitlab.mcone.eu", "pUwScza4PsZoFqPnVsNz");
    }

    public File getArtifact(int projectId, String artifactPath) throws GitLabApiException {
        String artifactName = new LinkedList<>(Arrays.asList(artifactPath.split("/"))).getLast();

        int oldPipeline = WrapperServer.getInstance().getConfig().getConfig().getSection("builds").getSection("gitlab").getInt(artifactName.replace('.', '-'));
        int latestPipeline = gitLabApi.getPipelineApi().getPipelines(projectId).iterator().next().getId();

        if (oldPipeline < latestPipeline) {
            List<Job> jobs = gitLabApi.getJobApi().getJobsForPipeline(projectId, latestPipeline, Constants.JobScope.SUCCESS);

            for (Job job : jobs) {
                if (job.getStage().equals("build") && job.getName().equals("build")) {
                    WrapperServer.getInstance().getConfig().getConfig().set("builds.gitlab."+artifactName.replace('.', '-'), latestPipeline);
                    WrapperServer.getInstance().getConfig().save();

                    Path path = Paths.get(artifactPath);
                    log.info("Downloading artifact "+artifactName+" from project with id "+projectId);
                    return gitLabApi.getJobApi().downloadSingleArtifactsFile(projectId, job.getId(), path, JAR_DIR);
                }
            }

            log.warning("Failed to download artifact "+artifactName+" from project with id "+projectId);
            return null;
        } else {
            log.info("Using cached jar for artifact "+artifactName+"...");
            return new File(JAR_DIR, artifactName);
        }
    }

}