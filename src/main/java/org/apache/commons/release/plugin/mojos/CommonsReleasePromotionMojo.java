/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.release.plugin.mojos;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.release.plugin.SharedFunctions;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * The purpose of this maven mojo is to promote staged releases from the dev staging area to the
 * releases area as required by the Apache release process.
 *
 * @author chtompki
 * @since 1.3
 */
@Mojo(name = "promote",
    threadSafe = true,
    aggregator = true)
public class CommonsReleasePromotionMojo extends AbstractMojo {

    /**
     * The maven project context injection so that we can get a hold of the variables at hand.
     */
    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    /**
     * The working directory in <code>target</code> that we use as a sandbox for the plugin.
     */
    @Parameter(defaultValue = "${project.build.directory}/commons-release-plugin",
        property = "commons.outputDirectory")
    private File workingDirectory;

    /**
     * The subversion staging url to which we upload all of our staged artifacts.
     */
    @Parameter(defaultValue = "", property = "commons.distSvnStagingUrl")
    private String distSvnStagingUrl;

    /**
     * The subversion release url to which we promote our previously staged artifacts.
     */
    @Parameter(defaultValue = "", property = "commons.distSvnReleaseUrl")
    private String distSvnReleaseUrl;

    /**
     * The location to which to checkout the dist subversion staging (dev) repository under our working directory, which
     * was given above.
     */
    @Parameter(defaultValue = "${project.build.directory}/commons-release-plugin/dist-staging-scm",
            property = "commons.distStagingCheckoutDirectory")
    private File distStagingCheckoutDirectory;

    /**
     * The location to which to checkout the dist release subversion repository under our working directory, which
     * was given above.
     */
    @Parameter(defaultValue = "${project.build.directory}/commons-release-plugin/dist-release-scm",
            property = "commons.distReleaseCheckoutDirectory")
    private File distReleaseCheckoutDirectory;

    /**
     * The username for the distribution subversion repository. This is typically your apache id.
     */
    @Parameter(property = "user.name")
    private String username;

    /**
     * The password associated with {@link CommonsDistributionStagingMojo#username}.
     */
    @Parameter(property = "user.password")
    private String password;


    /**
     * A parameter to generally avoid running unless it is specifically turned on by the consuming
     * module.
     */
    @Parameter(defaultValue = "false", property = "commons.release.isDistModule")
    private Boolean isDistModule;

    @Override
    public void execute() throws MojoExecutionException {
        if (!isDistModule) {
            getLog().info("This module is marked as a non distribution "
                    + "or assembly module, and the plugin will not run.");
            return;
        }
        if (StringUtils.isEmpty(distSvnStagingUrl)) {
            getLog().warn("commons.distSvnStagingUrl is not set, the commons-release-plugin will not run.");
            return;
        }
        if (!workingDirectory.exists()) {
            SharedFunctions.initDirectory(getLog(), workingDirectory);
        }
        getLog().info("Preparing to promote distributions from dev to releases.");
        try {
            ScmManager scmManager = new BasicScmManager();
            ScmRepository stagingRepository = SharedFunctions.buildScmRepository(scmManager, distSvnStagingUrl);
            ScmProvider stagingProvider = SharedFunctions.buildScmProvider(scmManager,
                                                                           stagingRepository,
                                                                           username,
                                                                           password);
            ScmRepository releasesRepository = SharedFunctions.buildScmRepository(scmManager, distSvnReleaseUrl);
            ScmProvider releasesProvider = SharedFunctions.buildScmProvider(scmManager,
                                                                            releasesRepository,
                                                                            username,
                                                                            password);
            initializeScmDirectories();
            ScmFileSet stagingScmFileSet = SharedFunctions.checkoutFiles(getLog(),
                    distStagingCheckoutDirectory, distSvnStagingUrl, stagingProvider, stagingRepository);
            ScmFileSet releaseScmFileSet = SharedFunctions.checkoutFiles(getLog(),
                    distReleaseCheckoutDirectory, distSvnReleaseUrl, releasesProvider, releasesRepository);
        } catch (ScmException e) {
            getLog().error("Could not promte files from: " + distSvnStagingUrl, e);
            throw new MojoExecutionException("Could not promte files from: " + distSvnStagingUrl, e);
        }
    }

    /**
     * If the directories under <code>./target/commons-release-plugin</code> into which we checkout the
     * dist staging svn locations don't exist we need to create them.
     *
     * @throws MojoExecutionException in the case that we have an {@link java.io.IOException} under the hood
     *                                in the creation of the directories.
     */
    private void initializeScmDirectories() throws MojoExecutionException {
        if (!distStagingCheckoutDirectory.exists()) {
            SharedFunctions.initDirectory(getLog(), distStagingCheckoutDirectory);
        }
        if (!distReleaseCheckoutDirectory.exists()) {
            SharedFunctions.initDirectory(getLog(), distReleaseCheckoutDirectory);
        }
    }
}
