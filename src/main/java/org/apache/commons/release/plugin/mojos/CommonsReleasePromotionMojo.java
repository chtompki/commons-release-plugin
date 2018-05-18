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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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
     * A parameter to generally avoid running unless it is specifically turned on by the consuming
     * module.
     */
    @Parameter(defaultValue = "false", property = "commons.release.isDistModule")
    private Boolean isDistModule;

    @Override
    public void execute() throws MojoExecutionException {

    }
}
