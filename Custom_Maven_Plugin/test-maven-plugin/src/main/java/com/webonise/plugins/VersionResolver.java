package com.webonise.plugins;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.apache.maven.artifact.repository.ArtifactRepository;

/**
 * Goal to resolve version of dependency and the version mentioned in dependents
 * 
 * @goal touch
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
	private MavenProject project;

	private static int incompatiblePomFileCount = 0;

	/**
	 * location of the local repository
	 * 
	 * @parameter default-value="${localRepository}"
	 */
	ArtifactRepository localRepository;

	public static String targetArtifact;
	public static String targetVersion;

	/**
	 * default execute method of AbstractMojo class
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		

		VersionResolver versionResolver=new VersionResolver();
		
		versionResolver.printProjectInfo(project);
		File repository=versionResolver.getBaseDirectoryPath(localRepository);
		
		versionResolver.findPomFile(repository);

		// setting the values of current project's artifact and version
		targetArtifact = project.getArtifactId();
		targetVersion = project.getVersion();

		
	}

	/**
	 * Method resets the model object of VersionResolver class to point to the
	 * artifact represented by the pom file passed as an argument. Method also
	 * skips the incompatible pom files
	 * 
	 * @param newPomFile
	 *            : File object of the pom file
	 */
	@SuppressWarnings("finally")
	Model resetModelObject(File newPomFile, Model model) {

		/** Object to parse the pom File and return a Model */
		MavenXpp3Reader xmlReader = new MavenXpp3Reader();
		try {
			model = xmlReader.read(new FileReader(newPomFile));
			model.setPomFile(newPomFile);

		} catch (XmlPullParserException e) {
			incompatiblePomFileCount++;
		} catch (IOException e) {
			// skipping incompatible .pom files....
		} finally {
			return model;
		}
	}

	/**
	 * accepting the pom file and checking target project's artifact id with all
	 * the artifact id's found in pom
	 * 
	 * @param model
	 *            : Model object of the Project
	 * @throws IOException
	 */
	public void resolveDependencyVersion(Model model) {
		try {
			project = new MavenProject(model);

			@SuppressWarnings("unchecked")
			List<Dependency> dependencies = project.getDependencies();
			// Iterating all the dependency in the pom file
			for (Dependency currentDependency : dependencies) {
				String artifact = currentDependency.getArtifactId();
				String version = currentDependency.getVersion();
				ComparableVersion target = new ComparableVersion(targetVersion);
				// if the target dependency is present in the current pom file
				// AND the version is in range
				if (version != null && artifact.equals(targetArtifact)
						&& version.matches("(\\[|\\()(.*)(\\]|\\))")) {
					// invoking method that checks if target version is in the
					// dependency version range
					// version.split(",")[0] returns substring before ',' i.e.
					// Min version
					// version.split(",")[1] returns substring after ',' i.e.
					// Max version
					this.checkVersionCompatiblity(target,
							version.split(",")[0], version.split(",")[1]);
				}

				// if the target dependency is present in the current pom file
				else if (artifact.equals(targetArtifact)) {
					this.checkVersionCompatiblity(target, version);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Method checks if version of the target project is equal to version
	 * mentioned as dependency in any other dependent project.
	 * 
	 * @param targetVersion
	 *            : ComparableVersion object of target project
	 * @param dependencyVersion
	 *            : String containing version of the dependency
	 */
	void checkVersionCompatiblity(ComparableVersion targetVersion,
			String dependencyVersion) {
		ComparableVersion dependent = new ComparableVersion(dependencyVersion);
		// if the target version is equal to dependency version
		if (targetVersion.compareTo(dependent) != 0) {
			getLog().error(
					"DEPENDENCY VERSION MISMATCH. please check version of dependency in "
							+ project.getGroupId() + "."
							+ project.getArtifactId());
			throw new DependencyVersionMismatchError(getLog(),
					incompatiblePomFileCount);
		}
	}

	/**
	 * Method checks if version of the target project is within the range of
	 * version-range mentioned as dependency in any other dependent project.
	 * 
	 * @param targetVersion
	 *            : ComparableVersion object of target project
	 * @param dependencyMinVersion
	 *            : String containing minimum version starting by '[' or '('
	 *            representing bound inclusive or exclusive
	 * @param dependencyMaxVersion
	 *            : String containing maximum version ended by ']' or ')'
	 *            representing bound inclusive or exclusive
	 */
	void checkVersionCompatiblity(ComparableVersion targetVersion,
			String dependencyMinVersion, String dependencyMaxVersion) {
		/**
		 * flags denoting weather target version is in range from minimum
		 * version side and maximum version side respectively
		 */
		boolean minimumInRange = false, maximumInRange = false;

		/**
		 * flags denoting weather minimum and maximum versions are inclusive or
		 * exclusive. By default set to true
		 */
		boolean minBoundInclusive = true, maxBoundInclusive = true;

		// if minimum version is excluded
		if (dependencyMinVersion.contains("(")) {
			minBoundInclusive = false;
		}
		// if maximum version is excluded
		if (dependencyMaxVersion.contains(")")) {
			maxBoundInclusive = false;
		}
		// removing braces from the version Strings and creating
		// ComparableVersion objects
		ComparableVersion minVersion = new ComparableVersion(
				dependencyMinVersion.replaceAll("\\[|\\(", ""));
		ComparableVersion maxVersion = new ComparableVersion(
				dependencyMaxVersion.replaceAll("\\]|\\)", ""));

		// checking for minimum bound
		if (minBoundInclusive && targetVersion.compareTo(minVersion) >= 0) {
			minimumInRange = true;
		} else if (targetVersion.compareTo(minVersion) > 0) {
			minimumInRange = true;
		} else {
			minimumInRange = false;
		}
		// checking for maximum bound
		if (maxBoundInclusive && targetVersion.compareTo(maxVersion) <= 0) {
			maximumInRange = true;
		} else if (targetVersion.compareTo(maxVersion) < 0) {
			maximumInRange = true;
		} else {
			maximumInRange = false;
		}
		// if any one of the minimumInRange and maximumInRange flags is false
		if (!(minimumInRange & maximumInRange)) {
			getLog().error(
					"DEPENDENCY VERSION MISMATCH. please check version of dependency in "
							+ project.getGroupId() + "."
							+ project.getArtifactId());
			throw new DependencyVersionMismatchError(getLog(),
					incompatiblePomFileCount);
		}
	}
	
	void printProjectInfo(MavenProject project)
	{
		getLog().info("Printing Current Project's Artifact ID & Version");
		getLog().info("Project Version: " + project.getVersion().toString());
		getLog().info("Artifact ID " + project.getArtifactId().toString());
		
	}
	
	File getBaseDirectoryPath(ArtifactRepository localRepository)
	{
		// String localPath=localRepository.getBasedir();
		getLog().info("LocalRepository Path:" + localRepository.getBasedir());

		// getting the file object for local repository
		File repository = new File(localRepository.getBasedir().concat("/"));
		getLog().info(repository.getAbsolutePath());
		return repository;
	}
	
	void findPomFile(File repository)
	{
		/** Model object to represent project from a pom file */
		Model model = new Model();
		// getting all the pom File object list in local repository
				@SuppressWarnings("unchecked")
				Collection<File> pomFiles = FileUtils.listFiles(repository,
						new String[] { "pom" }, true);
				for (File currentFile : pomFiles) {
					// setting the Model object for the current pom file
					model = this.resetModelObject(currentFile, model);

					// method invocation to resolve the version of dependency in the pom
					this.resolveDependencyVersion(model);
				}
				getLog().info(
						"skipped " + incompatiblePomFileCount
								+ " incompatible pom files in your repository.");
	}
}