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
package org.apache.commons.release.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Shared static functions for all of our Mojos.
 *
 * @author chtompki
 * @since 1.0
 */
public final class SharedFunctions {

    /**
     * I want a buffer that is an array with 1024 elements of bytes. We declare
     * the constant here for the sake of making the code more readable.
     */
    public static final int BUFFER_BYTE_SIZE = 1024;

    /**
     * Making the constructor private because the class only contains static methods.
     */
    private SharedFunctions() {
        // Utility Class
    }

    /**
     * Cleans and then initializes an empty directory that is given by the <code>workingDirectory</code>
     * parameter.
     *
     * @param log is the Maven log for output logging, particularly in regards to error management.
     * @param workingDirectory is a {@link File} that represents the directory to first attempt to delete then create.
     * @throws MojoExecutionException when an {@link IOException} or {@link NullPointerException} is caught for the
     *      purpose of bubbling the exception up to Maven properly.
     */
    public static void initDirectory(Log log, File workingDirectory) throws MojoExecutionException {
        if (workingDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(workingDirectory);
            } catch (IOException | NullPointerException e) {
                final String message = String.format("Unable to remove directory %s: %s", workingDirectory,
                        e.getMessage());
                log.error(message);
                throw new MojoExecutionException(message, e);
            }
        }
        if (!workingDirectory.exists()) {
            workingDirectory.mkdirs();
        }
    }

    /**
     * Copies a {@link File} from the <code>fromFile</code> to the <code>toFile</code> and logs the failure
     * using the Maven {@link Log}.
     *
     * @param log the {@link Log}, the maven logger.
     * @param fromFile the {@link File} from which to copy.
     * @param toFile the {@link File} to which to copy into.
     * @throws MojoExecutionException if an {@link IOException} or {@link NullPointerException} is caught.
     */
    public static void copyFile(Log log, File fromFile, File toFile) throws MojoExecutionException {
        try {
            FileUtils.copyFile(fromFile, toFile);
        } catch (IOException | NullPointerException e) {
            final String message = String.format("Unable to copy file %s tp %s: %s", fromFile, toFile, e.getMessage());
            log.error(message);
            throw new MojoExecutionException(message, e);
        }
    }

    /**
     * Builds up an {@link ScmProvider} for an {@link SvnScmProviderRepository}from a url, a username, and
     * a password.
     *
     * @param scmManager is the project's {@link ScmManager}.
     * @param repository is the initialized {@link ScmRepository}.
     * @param username the username for the repository.
     * @param password the password for the repository.
     * @return an initialized {@link ScmProvider}.
     * @throws ScmException if the creation fails.
     */
    public static ScmProvider buildScmProvider(ScmManager scmManager, ScmRepository repository,
                                                    String username, String password) throws ScmException {
        ScmProvider provider = scmManager.getProviderByRepository(repository);
        SvnScmProviderRepository providerRepository = (SvnScmProviderRepository) repository.getProviderRepository();
        providerRepository.setUser(username);
        providerRepository.setPassword(password);
        return provider;
    }

    /**
     * Builds up the {@link ScmRepository} to be used in committing to and from SVN.
     *
     * @param scmManager the maven {@link ScmManager} for the project.
     * @param scmUrl the url for the repository in the form <code>scm:svn:https://TheRemainderOfTheSvnRepoUrl</code>.
     * @return the properly configured {@link ScmRepository}.
     * @throws ScmException if an error in the checkout occurrs.
     */
    public static ScmRepository buildScmRepository(ScmManager scmManager, String scmUrl) throws ScmException {
        scmManager.setScmProvider("svn", new SvnExeScmProvider());
        return scmManager.makeScmRepository(scmUrl);
    }

    /**
     * Convenience method for checking out the svn repository and log the fact that we do so.
     *
     * @param log the maven {@link Log} for the sake of logging that we're doing an SVN checkout.
     * @param checkoutDirectory the {@link File} to which we checkout the
     * @param scmUrl the url from which we are checking out the SVN repo for the sake of logging.
     * @param scmProvider the {@link ScmProvider} to use for the checkout.
     * @param repository the {@link ScmRepository} to use for the checkout.
     * @return the {@link ScmFileSet} that has been checked out.
     * @throws ScmException if an error in the checkout occurrs.
     */
    public static ScmFileSet checkoutFiles(Log log, File checkoutDirectory, String scmUrl,
                                     ScmProvider scmProvider, ScmRepository repository) throws ScmException {
        ScmFileSet scmFileSet = new ScmFileSet(checkoutDirectory);
        log.info("Checking out dist from: " + scmUrl);
        scmProvider.checkOut(repository, scmFileSet);
        return scmFileSet;
    }
}
