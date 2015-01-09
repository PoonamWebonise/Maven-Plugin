package com.webonise.plugins;

import java.io.File;
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

		public void compareVersions(String actualVersion,String dependencyVersion) throws MojoFailureException 
	{
		if(!actualVersion.equalsIgnoreCase(dependencyVersion))
		{
		throw new MojoFailureException("Dependency Version Mismatch in one of the Dependants. Please update xml and retry.");
		}
	}
		//accepting the pom file and checking current project's artifact id with all the artifact id's found in pom
	public void getPomObject(File pomfile, String current_project_artifact) {

		MavenProject project;
		Model model = null;
		FileReader reader = null;
		MavenXpp3Reader mavenreader = new MavenXpp3Reader();
		try {
			reader = new FileReader(pomfile);
			model = mavenreader.read(reader);
			model.setPomFile(pomfile);

			project = new MavenProject(model);
			project.getProperties();
			@SuppressWarnings("unchecked")
			String actualVersion = project.getVersion();
			String dependencyVersion; 
			List<Dependency> dependencies = project.getDependencies();
			Iterator<Dependency> myIterator = dependencies.iterator();
			String artifact = "";
			String version = "";
			while (myIterator.hasNext()) {
				Dependency current = myIterator.next();
				artifact = current.getArtifactId();
				version = current.getVersion();
				if (artifact.equals(current_project_artifact)) {
					getLog().info("pom file:" + pomfile);
					getLog().info(
							"Dependency Artifact ID: " + artifact
									+ "\tVersion: " + version);
					dependencyVersion = version;
					this.compareVersions(actualVersion,dependencyVersion);
				}
			}
		}
		catch (Exception ex) {
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub

	}
}
