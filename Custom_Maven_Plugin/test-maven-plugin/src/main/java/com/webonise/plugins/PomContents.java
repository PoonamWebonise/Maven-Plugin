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
		getLog().info(
				"Version & Artifact Id's of Dependencies found in Pom.xml");
		getLog().info("Project Version: " + project.getVersion().toString());
		getLog().info("Project Dependencies: ");
		@SuppressWarnings("unchecked")
		List<Dependency> dependencies = project.getDependencies();
		Iterator<Dependency> myIterator = dependencies.iterator();
		String artifact = "";
		while (myIterator.hasNext()) {
			Dependency current = myIterator.next();
			getLog().info(
					"Artifact ID: " + current.getArtifactId() + "\tVersion: "
							+ current.getVersion());
			artifact = current.getArtifactId();
		}
		getLog().info("Current Artifact ID: " + artifact);
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub

	}
}
