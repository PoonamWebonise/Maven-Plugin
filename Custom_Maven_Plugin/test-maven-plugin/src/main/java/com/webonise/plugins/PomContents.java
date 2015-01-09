package com.webonise.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class PomContents extends AbstractMojo {

	// accepting the pom file and checking current project's artifact id with
	// all the artifact id's found in pom
	public void getPomObject(File pomfile, String currentProjectArtifact,
			String currentProjectVersion) {

		MavenProject project;
		
		
		MavenXpp3Reader mavenreader = new MavenXpp3Reader();
		try {
			FileReader reader = new FileReader(pomfile);
			Model model = mavenreader.read(reader);
			model.setPomFile(pomfile);

			project = new MavenProject(model);
			project.getProperties();
			@SuppressWarnings("unchecked")
			List<Dependency> dependencies = project.getDependencies();
			Iterator<Dependency> dependencyIterator = dependencies.iterator();
			String artifact = "";
			String version = "";
			while (dependencyIterator.hasNext()) {
				Dependency current = dependencyIterator.next();
				artifact = current.getArtifactId();
				version = current.getVersion();
				if (artifact.equals(currentProjectArtifact)) {
					getLog().info("pom file:" + pomfile);
					getLog().info("Dependency Artifact ID: " + artifact+ "\tVersion: " + version);
					if (!currentProjectVersion.equalsIgnoreCase(version))
					{
						getLog().error("-----------------------------------------------------------------------");
						getLog().error("DEPENDENCY VERSION MISMATCH in one of the Dependants. Please update xml and retry.");
						getLog().error("-----------------------------------------------------------------------");
					} else
						getLog().info("Current Version: " + currentProjectVersion+"Version found in pom: " + version);
				}
			}
		}
		catch (FileNotFoundException ex)
		{
			getLog().error("pom File not found");
		}
		catch (Exception ex)
		{
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub

	}
}
         