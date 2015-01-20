package com.webonise.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Goal to resolve version of dependency and the
 * version mentioned in dependents
 * 
 * @goal touch
 * @phase compile
 * @author webonise
 */
public class VersionResolver extends AbstractMojo
{
	/**
	 * Maven Project entity with default value as current project 
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 */
	MavenProject project;
	
	/** Model object to represent project from a pom file*/
	private Model model;
	
	/** Object to parse the pom File and return a Model*/
	private MavenXpp3Reader xmlReader;
	
	/**
	 * location of the local repository
	 * 
	 *  @parameter default-value="${localRepository}" 
	 */
	org.apache.maven.artifact.repository.ArtifactRepository localRepository;
	
	public static String targetArtifact;
	public static String targetVersion;
	
	/**default execute method of AbstractMojo class
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		this.xmlReader = new MavenXpp3Reader();
		
		getLog().info("Printing Current Project's Artifact ID & Version");
		getLog().info("Project Version: " + project.getVersion().toString());
		getLog().info("Artifact ID " + project.getArtifactId().toString());
		getLog().info("LocalRepository Path:" + localRepository.getBasedir());
		
		//getting the file object for local repository
		File repository = new File(localRepository.getBasedir().concat("/"));
		getLog().info(repository.getAbsolutePath());
		
		//setting the values of current project's artifact and version
		targetArtifact=project.getArtifactId();
		targetVersion=project.getVersion();
		
		//method invocation to scan for all .pom files in the repository
		this.findPomModels(repository);
	}
	
	/**Method finds all the .pom files in the directory 
	 * recursively in every sub-directory
	 * 
	 * @param directoryFile
	 */
	public void findPomModels(File directoryFile)
	{
		
		//filter returning all the pom files in the present directory
		FileFilter pomFileFilter = new FileFilter() {
			public boolean accept(File file)
			{
				return (file.isFile()&&file.getName().endsWith(".pom"));
			}
		};
		
		if(directoryFile.listFiles(pomFileFilter).length!=0)
		{
			for (File currentFile : directoryFile.listFiles(pomFileFilter))
			{
				try
				{
					//setting the Model object for the current pom file
					this.model = this.xmlReader.read(new FileReader(currentFile));
					this.model.setPomFile(currentFile);
					this.model.getUrl();
					
					//method invocation to resolve the version of dependency in the pom
					this.resolveDependencyVersion(this.model);
				}
				catch (XmlPullParserException e)
				{
					// skipping incompatible .pom files....
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		//filter returning all the sub-directories in the passed directory
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file)
			{
				return file.isDirectory();
			}
		};
		
		//calling the same method recursively for sub-directories
		for (File currentFile : directoryFile.listFiles(directoryFilter))
		{
				findPomModels(currentFile);
		}
	}
	
	/**accepting the pom file and checking target project's artifact id with
	 * all the artifact id's found in pom
	 * 
	 * @param model : Model object of the Project
	 * @throws IOException
	 */
	public void resolveDependencyVersion(Model model) throws IOException
	{
		try
		{
			project = new MavenProject(model);

			@SuppressWarnings("unchecked")
			List<Dependency> dependencies = project.getDependencies();
			//Iterating all the dependency in the pom file
			for(Dependency currentDependency:dependencies)
			{
				String artifact = currentDependency.getArtifactId();
				String version = currentDependency.getVersion();
				ComparableVersion target = new ComparableVersion(targetVersion);
				//if the target dependency is present in the current pom file AND the version is in range
				if(artifact.equals(targetArtifact)&&version.matches("(\\[|\\()(.*)(\\]|\\))"))
				{
					//invoking method that checks if target version is in the dependency version range
					//version.split(",")[0] returns substring before ',' i.e. Min version
					//version.split(",")[1] returns substring after ',' i.e. Max version
					this.checkVersionCompatiblity(target, version.split(",")[0], version.split(",")[1]);
				}
				
				//if the target dependency is present in the current pom file
				else if(artifact.equals(targetArtifact))
				{
					this.checkVersionCompatiblity(target, version);
				}
			}
		}
		catch(NullPointerException e)
		{
			//skipping the dependencies without version info, hence generating NullPointerException
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**Method checks if version of the target project is equal
	 * to version mentioned as dependency in any other 
	 * dependent project.
	 * 
	 * @param targetVersion : ComparableVersion object of target project
	 * @param dependencyVersion : String containing version of the dependency 
	 */
	void checkVersionCompatiblity(ComparableVersion targetVersion,String dependencyVersion)
	{
		ComparableVersion dependent = new ComparableVersion(dependencyVersion);
		//if the target version is equal to dependency version
		if(targetVersion.compareTo(dependent)!=0)
		{
			getLog().error("DEPENDENCY VERSION MISMATCH. please check version of dependency in "+project.getGroupId()+"."+project.getArtifactId());
			throw new DependencyVersionMismatchError(getLog());
		}
	}
	
	/**Method checks if version of the target project is within the
	 * range of version-range mentioned as dependency in any other 
	 * dependent project.
	 * 
	 * @param targetVersion : ComparableVersion object of target project
	 * @param dependencyMinVersion : String containing minimum version starting by '[' or '(' representing bound inclusive or exclusive
	 * @param dependencyMaxVersion : String containing minimum version ended by ']' or ')' representing bound inclusive or exclusive
	 */
	void checkVersionCompatiblity(ComparableVersion targetVersion, String dependencyMinVersion,String dependencyMaxVersion)
	{
		/**flags denoting weather target version is in range 
		 * from minimum version side and maximum version side respectively
		 */ 
		boolean minimumInRange,maximumInRange;
		
		/**flags denoting weather minimum and maximum versions 
		 * are inclusive or exclusive. By default set to true
		 */
		boolean minBoundInclusive = true, maxBoundInclusive = true;
		
		//if minimum version is excluded
		if(dependencyMinVersion.contains("("))
			minBoundInclusive=false;
		
		//if maximum version is excluded
		if(dependencyMaxVersion.contains(")"))
			maxBoundInclusive=false;
		
		//removing braces from the version Strings
		dependencyMinVersion=dependencyMinVersion.replace("[", "");
		dependencyMinVersion=dependencyMinVersion.replace("(", "");
		dependencyMaxVersion=dependencyMaxVersion.replace("]", "");
		dependencyMaxVersion=dependencyMaxVersion.replace(")", "");
		
		//creating ComparableVersion objects
		ComparableVersion minVersion = new ComparableVersion(dependencyMinVersion);
		ComparableVersion maxVersion = new ComparableVersion(dependencyMaxVersion);
		
		//checking for minimum bound
		if(minBoundInclusive&&targetVersion.compareTo(minVersion)>=0)
			minimumInRange = true;
		else if(targetVersion.compareTo(minVersion)>0)
			minimumInRange = true;
		else
			minimumInRange = false;
		
		//checking for maximum bound
		if(maxBoundInclusive&&targetVersion.compareTo(maxVersion)<=0)
			maximumInRange = true;
		else if(targetVersion.compareTo(maxVersion)<0)
			maximumInRange = true;
		else
			maximumInRange = false;
		
		//if any one of the minimumInRange and maximumInRange flags is false
		if(!(minimumInRange & maximumInRange))
		{
			getLog().error("DEPENDENCY VERSION MISMATCH. please check version of dependency in "+project.getGroupId()+"."+project.getArtifactId());
			throw new DependencyVersionMismatchError(getLog());
		}
	}
}
