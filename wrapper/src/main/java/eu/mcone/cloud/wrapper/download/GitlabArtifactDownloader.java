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
        //Private Token Dominik.Lippl
        gitLabApi = new GitLabApi("https://gitlab.onegaming.group", " eCktFWAXQEi7BsWyLRpv");
    }

    public File getArtifact(int projectId, String artifactPath) throws GitLabApiException {
        String artifactName = new LinkedList<>(Arrays.asList(artifactPath.split("/"))).getLast();
        System.out.println("Artifact Name: " + artifactName);
        System.out.println("Path: " + artifactPath);

        int oldPipeline = WrapperServer.getInstance().getConfig().getConfig().getSection("builds").getSection("gitlab").getInt(artifactName.replace('.', '-'));
        int latestPipeline = gitLabApi.getPipelineApi().getPipelines(projectId).iterator().next().getId();

        System.out.println("Old pipe: " + oldPipeline);
        System.out.println("latest: " + latestPipeline);

        if (oldPipeline < latestPipeline) {
            List<Job> jobs = gitLabApi.getJobApi().getJobsForPipeline(projectId, latestPipeline, Constants.JobScope.SUCCESS);
            System.out.println("Jobs: " + jobs);

            for (Job job : jobs) {
                System.out.println("Job name: " + job.getName());
                System.out.println("Job stage: " + job.getStage());
                if (job.getStage().equals("build") && job.getName().equalsIgnoreCase("validate:jdk8")) {
                    System.out.println("DEBUG-1");
                    WrapperServer.getInstance().getConfig().getConfig().set("builds.gitlab." + artifactName.replace('.', '-'), latestPipeline);
                    WrapperServer.getInstance().getConfig().save();

                    Path path = Paths.get(artifactPath);
                    Path test = Paths.get(job.getWebUrl());

                    System.out.println("DEBUG-Path: " + path);
                    System.out.println("Web path: " + test);
                    log.info("Downloading artifact " + artifactName + " from project with id " + projectId);
                    return gitLabApi.getJobApi().downloadSingleArtifactsFile(projectId, job.getId(), test, JAR_DIR);
                }
            }

            log.warning("Failed to download artifact " + artifactName + " from project with id " + projectId);
            return null;
        } else {
            log.info("Using cached jar for artifact " + artifactName + "...");
            return new File(JAR_DIR, artifactName);
        }
    }

}