package com.webonise.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.versioning.*;

/**
 * Goal to resolve version of dependency and the version mentioned in dependents
 * 
 * @goal touch
 * 
 * @phase compile
 * @author webonise
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
	 * @parameter default-value="${localRepository}"
	 */
	org.apache.maven.artifact.repository.ArtifactRepository localRepository;

	public static String currentArtifact;
	public static String currentVersion;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Printing Current Project's Artifact ID & Version");
		getLog().info("Project Version: " + project.getVersion().toString());
		getLog().info("Artifact ID " + project.getArtifactId().toString());
		getLog().info("LocalRepository Path:" + localRepository.getBasedir());

		// getting the file object for local repository
		File repository = new File(localRepository.getBasedir().concat("/"));

		getLog().info(repository.getAbsolutePath());

		// setting the values of current project's artifact and version
		currentArtifact = project.getArtifactId();
		currentVersion = project.getVersion();

		// method invocation to scan for all .pom files in the repository
		PomFileFinder pomfile = new PomFileFinder();
		pomfile.findPomModels(repository);

	}

	/**
	 * accepting the pom file and checking current project's artifact id with
	 * all the artifact id's found in pom
	 * 
	 * @param model
	 * @throws IOException
	 */
	public void resolveDependencyVersion(Model model) throws IOException {
		try {
			// Model model = new MavenXpp3Reader().read(new
			// FileReader(pomfile));
			// model.setPomFile(pomfile);

			project = new MavenProject(model);
			// String path= project.getModel();
			@SuppressWarnings("unchecked")
			List<Dependency> dependencies = project.getDependencies();
			Iterator<Dependency> dependencyIterator = dependencies.iterator();

			// Iterating all the dependency in the pom file
			while (dependencyIterator.hasNext()) {
				Dependency current = dependencyIterator.next();
				String artifact = current.getArtifactId();
				String version = current.getVersion();
				// String path=current.getSystemPath();
				// if the target dependency is present in the current pom file
				if (artifact.equals(currentArtifact)) {
					getLog().info(
							"Dependency Artifact ID: " + artifact
									+ "\tVersion: " + version
									+ "\tPOMFile Name :" + project.getName());

					// if version of the target dependency is unequal to version
					// mentioned in the pom of the dependent
					ComparableVersion foundVersion = new ComparableVersion(
							currentVersion);
					ComparableVersion neededVersion = new ComparableVersion(
							version);
					int versionStatus = foundVersion.compareTo(neededVersion);
					if (versionStatus == 0)
						getLog().info(
								"\tVERSION MATCHED! \nCurrent Version: "
										+ currentVersion
										+ "\tVersion found in pom: " + version);
					else
						getLog().error(
								"DEPENDENCY VERSION MISMATCH in one of the Dependants. Please update xml and retry.");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
