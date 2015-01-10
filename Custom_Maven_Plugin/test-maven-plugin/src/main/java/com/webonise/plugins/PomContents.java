package com.webonise.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal touch
 * 
 * @phase compile
 */
public class PomContents extends AbstractMojo {

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
		
		File repository = new File(localRepository.getBasedir().concat("/"));
		try {
			getLog().info(repository.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PomFileFinder pomfile = new PomFileFinder();
		pomfile.findSubDirectories(repository,project.getArtifactId(),project.getVersion());

	}

	// accepting the pom file and checking current project's artifact id with
	// all the artifact id's found in pom
	public void getPomObject(File pomfile, String currentProjectArtifact, String currentProjectVersion) {
		
		

		try {
			Model model = new MavenXpp3Reader().read(new FileReader(pomfile));
			model.setPomFile(pomfile);

			project = new MavenProject(model);
			project.getProperties();
			@SuppressWarnings("unchecked")
			List<Dependency> dependencies = project.getDependencies();
			Iterator<Dependency> dependencyIterator = dependencies.iterator();
			while (dependencyIterator.hasNext()) {
				Dependency current = dependencyIterator.next();
				String artifact = current.getArtifactId();
				String version = current.getVersion();
				if (artifact.equals(currentProjectArtifact)) {
					getLog().info("pom file:" + pomfile);
					getLog().info(
							"Dependency Artifact ID: " + artifact
									+ "\tVersion: " + version);
					if (!currentProjectVersion.equalsIgnoreCase(version)) {

						getLog().error(
								"DEPENDENCY VERSION MISMATCH in one of the Dependants. Please update xml and retry.");
					} else
						getLog().info(
								"Current Version: " + currentProjectVersion
										+ "Version found in pom: " + version);
				}
			}
		} catch (FileNotFoundException ex) {
			getLog().error("pom File not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
