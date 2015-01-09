package com.webonise.plugins;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal touch
 * 
 * @phase process-sources
 */
public class MyMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 */
	MavenProject project;
	/** @parameter default-value="${localRepository}" */
	org.apache.maven.artifact.repository.ArtifactRepository localRepository;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Printing Current Project's Artifact ID & Version");
		getLog().info("Project Version: " + project.getVersion().toString());
		getLog().info("Artifact ID " + project.getArtifactId().toString());
		getLog().info("LocalRepository Path:" + localRepository.getBasedir());

		String repository_path = localRepository.getBasedir().concat("/");
		FindPom pomfile = new FindPom();
		pomfile.findRepository(repository_path);
	}
}
