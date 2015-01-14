package com.webonise.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Goal to resolve version of dependency and the
 * version mentioned in dependents
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
		
		//getting the file object for local repository
		File repository = new File(localRepository.getBasedir().concat("/"));
		getLog().info(repository.getAbsolutePath());
			
		//setting the values of current project's artifact and version
		currentArtifact=project.getArtifactId();
		currentVersion=project.getVersion();
		
		//method invocation to scan for all .pom files in the repository
		PomFileFinder pomfile = new PomFileFinder();
		pomfile.findPomModels(repository);

	}

	/**accepting the pom file and checking current project's artifact id with
	 * all the artifact id's found in pom
	 * 
	 * @param model
	 * @throws IOException
	 */
	public void resolveDependencyVersion(Model model) throws IOException
	{
		try
		{
			//Model model = new MavenXpp3Reader().read(new FileReader(pomfile));
			//model.setPomFile(pomfile);
			
			//'[' means inclusive i.e. including min/max '(' means exclusive i.e. excluding min/max
			
			project = new MavenProject(model);
			//String path= project.getModel();
			@SuppressWarnings("unchecked")
			List<Dependency> dependencies = project.getDependencies();
			Iterator<Dependency> dependencyIterator = dependencies.iterator();
			//Iterating all the dependency in the pom file
			while (dependencyIterator.hasNext())
			{
				Dependency current = dependencyIterator.next();
				String artifact = current.getArtifactId();
				String version = current.getVersion();
				ComparableVersion target = new ComparableVersion(currentVersion);
				//if the target dependency is present in the current pom file AND the version is in range
				if (artifact.equals(currentArtifact)&&version.matches("(\\[|\\()(.*)(\\]|\\))"))
				{

					String maxVersion = version.split(",")[1];
					String minVersion = version.split(",")[0];
					if(!this.isVersionCompatible(target, minVersion, maxVersion))
					{
						getLog().error("DEPENDENCY VERSION MISMATCH. please check version of dependency in "+project.getGroupId()+"."+project.getArtifactId());
						getLog().error("Dependency Artifact ID: " + artifact+ "\tVersion: " + version+"\tPOMFile Name :"+project.getName());
					}
				}
				
				//if the target dependency is present in the current pom file
				else if(artifact.equals(currentArtifact))
				{
					ComparableVersion dependent = new ComparableVersion(version);
					if(target.compareTo(dependent)!=0)
					{
						getLog().error("DEPENDENCY VERSION MISMATCH. please check version of dependency in "+project.getGroupId()+"."+project.getArtifactId());
						getLog().error("Dependency Artifact ID: " + artifact+ "\tVersion: " + version+"\tPOMFile Name :"+project.getName());
					}
				}
			}
		}
		catch(NullPointerException e)
		{
			//DO NOTHING!
			//We know this exception will come and don't want to populate console with stack-trace
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**Method returns true if version of the target project is within the
	 * range of version-range mentioned as dependency in any other 
	 * dependent project.
	 * 
	 * @param targetVersion
	 * @param dependencyMinVersion
	 * @param dependencyMaxVersion
	 * @return boolean
	 */
	boolean isVersionCompatible(ComparableVersion targetVersion, String dependencyMinVersion,String dependencyMaxVersion)
	{
		boolean minimumInRange=false;
		boolean maximumInRange = false;
		
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
		
		//checking for maximum bound
		if(maxBoundInclusive&&targetVersion.compareTo(maxVersion)<=0)
			maximumInRange = true;
		else if(targetVersion.compareTo(maxVersion)<0)
			maximumInRange = true;
		
		//return true if both the flags are set true
		return minimumInRange & maximumInRange;
	}
}
