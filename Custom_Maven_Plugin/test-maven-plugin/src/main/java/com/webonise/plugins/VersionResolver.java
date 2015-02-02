package com.webonise.plugins;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Goal to resolve version of dependency and the
 * version mentioned in dependents
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
	
	/**
	 * location of the local repository
	 * 
	 *  @parameter default-value="${localRepository}" 
	 */
	private ArtifactRepository localRepository;
	
	private static String targetArtifact;
	private static String targetVersion;
	private static int incompatiblePomFileCount=0;

	/**default execute method of AbstractMojo class
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
				
		
		/** Model object to represent project from a pom file*/
		Model model = new Model();
		
		//printing current project's info
		this.printProjectInfo(project);
		
		//getting the file object for local repository
		File repository = this.getRepositoryFile(localRepository);
		
		//setting the values of current project's artifact and version
		targetArtifact=project.getArtifactId();
		targetVersion=project.getVersion();

		//getting all the pom File object list in local repository
		@SuppressWarnings("unchecked")
		Collection<File> pomFiles = FileUtils.listFiles(repository, new String[]{"pom"}, true);
		for (File currentFile : pomFiles)
		{
			// setting the Model object for the current pom file
			model = this.resetModelObject(currentFile, model);
			try
			{
				// method invocation to resolve the version of dependency in the pom
				this.resolveDependencyVersion(model);
				model.getProjectDirectory().getAbsoluteFile();
			}
			catch (NullPointerException e)
			{
				getLog().info("skipping null model object");
			}
		}
		getLog().info("skipped "+incompatiblePomFileCount + " incompatible pom files in your repository.");
	}
	
	/**Method prints general information about the supplied MavenProject
	 * to the default Log screen
	 * @param project : MavenProject object whose info is to be printed
	 */
	private void printProjectInfo(MavenProject project)
	{
		getLog().info("Printing Current Project's Artifact ID & Version");
		getLog().info("Project Version: " + project.getVersion().toString());
		getLog().info("Artifact ID " + project.getArtifactId().toString());
	}
	
	private File getRepositoryFile(ArtifactRepository localRepository)
	{
		// String localPath=localRepository.getBasedir();
		getLog().info("LocalRepository Path:" + localRepository.getBasedir());

		// getting the file object for local repository
		File repository = new File(localRepository.getBasedir().concat("/"));
		return repository;
	}
	
	/**Method resets the model object of VersionResolver class
	 * to point to the artifact represented by the pom file 
	 * passed as an argument. Method also skips the incompatible pom files
	 * 
	 * @param newPomFile : File object of the pom file
	 */
	@SuppressWarnings("finally")
	private Model resetModelObject(File newPomFile,Model model)
	{
		/** Object to parse the pom File and return a Model*/
		MavenXpp3Reader xmlReader = new MavenXpp3Reader();
		try
		{
			model = xmlReader.read(new FileReader(newPomFile));
			model.setPomFile(newPomFile);		
		}
		catch (XmlPullParserException e)
		{
			incompatiblePomFileCount++;
		}
		catch (IOException e)
		{
			getLog().error(e);
			
		}
		finally
		{
			return model;
		}
	}
	
	/**accepting the pom file and checking target project's artifact id with
	 * all the artifact id's found in pom
	 * 
	 * @param model : Model object of the Project
	 * @throws IOException
	 */
	private void resolveDependencyVersion(Model model)
	{
		try
		{
			project = new MavenProject(model);
			
			final String versionRangeRegex = "(\\[|\\()(.*)(\\]|\\))";
			@SuppressWarnings("unchecked")
			List<Dependency> dependencies = project.getDependencies();
			//Iterating all the dependency in the pom file
			for(Dependency currentDependency:dependencies)
			{
				String artifact = currentDependency.getArtifactId();
				String version = currentDependency.getVersion();
				ComparableVersion target = new ComparableVersion(targetVersion);
				if(version!=null && artifact.equals(targetArtifact) && version.matches(versionRangeRegex))
				{
					String[] rangeBounds = version.split(",");
					//invoking method that checks if target version is in the dependency version range
					this.checkVersionCompatiblity(target, rangeBounds[0], rangeBounds[1]);
				}
				
				//if the target dependency is present in the current pom file
				else if(artifact.equals(targetArtifact))
				{
					this.checkVersionCompatiblity(target, version);
				}
			}
		}
		catch (Exception ex)
		{
			getLog().error(ex);
		}
	}
	
	/**Method checks if version of the target project is equal
	 * to version mentioned as dependency in any other 
	 * dependent project.
	 * 
	 * @param targetVersion : ComparableVersion object of target project
	 * @param dependencyVersion : String containing version of the dependency 
	 */
	private void checkVersionCompatiblity(ComparableVersion targetVersion,String dependencyVersion)
	{
		ComparableVersion dependent = new ComparableVersion(dependencyVersion);
		//if the target version is equal to dependency version
		if(targetVersion.compareTo(dependent)!=0)
		{
				getLog().warn("DEPENDENCY VERSION MISMATCH. please check version of dependency in "
						+project.getGroupId()+"."+project.getArtifactId());
				getLog().warn("Mismatched artifact URL: "+project.getModel().getPomFile().getAbsolutePath());
		}
	}
	
	/**Method checks if version of the target project is within the
	 * range of version-range mentioned as dependency in any other 
	 * dependent project.
	 * 
	 * @param targetVersion : ComparableVersion object of target project
	 * @param dependencyMinVersion : String containing minimum version starting by '[' or '(' representing bound inclusive or exclusive
	 * @param dependencyMaxVersion : String containing maximum version ended by ']' or ')' representing bound inclusive or exclusive
	 */
	private void checkVersionCompatiblity(ComparableVersion targetVersion, String dependencyMinVersion,String dependencyMaxVersion)
	{
		LimitVersion minBound = new LimitVersion(dependencyMinVersion);
		LimitVersion maxBound = new LimitVersion(dependencyMaxVersion);
	
		//if version is not in range from either minimum side or maximum side
		if(!(minBound.versionIsLessThan(targetVersion) & maxBound.versionIsMoreThan(targetVersion)))
		{
				getLog().warn("DEPENDENCY VERSION MISMATCH. please check version of dependency in "
						+project.getGroupId()+"."+project.getArtifactId());
				getLog().warn("Mismatched artifact URL: "+project.getModel().getPomFile().getAbsolutePath());	
		}
	}
}