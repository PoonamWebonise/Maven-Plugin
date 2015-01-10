package com.webonise.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
		System.out.println("***************************"+project.getVersion());
		
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
			//getting Model for the passed pom file
			Model model = new MavenXpp3Reader().read(new FileReader(pomfile));
			model.setPomFile(pomfile);
			
			//Getting the MavenProject from the Model
			project = new MavenProject(model);
			project.getProperties();
			
			//Calling function to resolve version of dependancy and the current project
			VersionResolver versionResolver = new VersionResolver();
			versionResolver.resolveDependencyVersion(project, getLog());
			
		} 
		catch (FileNotFoundException ex)
		{
			getLog().error("pom File not found");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (XmlPullParserException e)
		{
			e.printStackTrace();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
