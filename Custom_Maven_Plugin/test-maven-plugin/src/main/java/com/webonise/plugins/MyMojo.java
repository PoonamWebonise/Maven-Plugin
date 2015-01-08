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

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.*;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
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

	public void listf(File dir) {
		File[] fList = dir.listFiles();
		for (File file1 : fList) {
			if (file1.isFile()) {
				if (file1.getName().endsWith(".pom")) {
					getLog().info("pom file:" + file1);
					this.getPomObject(file1);
				}
			} else if (file1.isDirectory()) {
				listf(file1);
			}
		}
	}

	public void getPomObject(File pomfile) {

		MavenProject project;

		Model model = null;
		FileReader reader = null;
		MavenXpp3Reader mavenreader = new MavenXpp3Reader();

		try {
			reader = new FileReader(pomfile);
			model = mavenreader.read(reader);
			model.setPomFile(pomfile);
		} catch (Exception ex) {
		}

		project = new MavenProject(model);
		project.getProperties();
		getLog().info("this is not coming");
		getLog().info("Project Version: " + project.getVersion().toString());
		getLog().info("Project Dependencies: ");
		List<Dependency> dependencies = project.getDependencies();
		Iterator<Dependency> myIterator = dependencies.iterator();
		String artifact = "";
		String version = "";
		while (myIterator.hasNext()) {
			Dependency current = myIterator.next();
			getLog().info(
					"Artifact ID: " + current.getArtifactId() + "\tVersion: "
							+ current.getVersion());
			artifact = current.getArtifactId();
			version = current.getVersion();
		}
		getLog().info("Current Artifact ID: " + artifact);
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("this is not coming");
		getLog().info("Project Version: " + project.getVersion().toString());
		getLog().info("Artifact ID " + project.getArtifactId().toString());
		getLog().info("LocalRepository Path:" + localRepository.getBasedir());

		String path = localRepository.getBasedir().concat("/");

		File f = new File(path);
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		File[] files = f.listFiles(directoryFilter);

		for (File file1 : files) {
			if (file1.isDirectory()) {

				this.listf(file1);
			} else {

			}
		}

	}

}
