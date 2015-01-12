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
 * Goal to resolve version of dependency and the
 * version mentioned in dependents
 * 
 * @goal touch
 * 
 * @phase compile
 */

public class VersionResolver extends AbstractMojo {

	/**
	 * Maven Project entity with default value as current project 
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 */
	MavenProject project;
	
	/**
	 * location of the local repository
	 * 
	 *  @parameter default-value="${localRepository}" 
	 */
	org.apache.maven.artifact.repository.ArtifactRepository localRepository;
	
	public static String currentArtifact;
	public static String currentVersion;
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
		currentArtifact=project.getArtifactId();
		currentVersion=project.getVersion();
		pomfile.findSubDirectories(repository);

	}

	/**accepting the pom file and checking current project's artifact id with
	 * all the artifact id's found in pom
	 * 
	 * @param pomfile
	 * @throws IOException
	 */
	public void resolveDependencyVersion(File pomfile) throws IOException {
		
		

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
				if (artifact.equals(currentArtifact)) {
					getLog().info("pom file:" + pomfile);
					getLog().info(
							"Dependency Artifact ID: " + artifact
									+ "\tVersion: " + version);
					if (!currentVersion.equalsIgnoreCase(version)) {

						getLog().error(
								"DEPENDENCY VERSION MISMATCH in one of the Dependants. Please update xml and retry.");
					} else
						getLog().info(
								"Current Version: " + currentVersion
										+ "Version found in pom: " + version);
				}
			}
		}
		
		catch (FileNotFoundException ex)
		{
			getLog().error("pom File not found");
		}
		catch (XmlPullParserException e) {
			
			//e.printStackTrace();
			//getLog().info("skipping incompatible pom files....");
		}
		
	}

}
