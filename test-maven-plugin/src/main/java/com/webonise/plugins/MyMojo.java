package com.webonise.plugins;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal touch
 * 
 * @phase process-sources
 */
public class MyMojo extends AbstractMojo {
    /**
     * Location of the file.
     * 
     * @parameter default-value="${project}"
     * @required
     */
    MavenProject project;
    public void execute() throws MojoExecutionException , MojoFailureException{
        getLog().info("this is not coming");
        getLog().info("Project Version: " + project.getVersion().toString());
        getLog().info("Project Dependencies: ");
        List<Dependency> dependencies = project.getDependencies();
        Iterator<Dependency> myIterator = dependencies.iterator();
        String artifact="";
        String version="";
        while (myIterator.hasNext()) {
            Dependency current = myIterator.next();
            getLog().info(
                    "Artifact ID: " + current.getArtifactId() + "\tVersion: "
                            + current.getVersion());
            artifact=current.getArtifactId();
            version=current.getVersion();
        }
        getLog().info("Current Artifact ID: "+artifact); 
        BufferedReader br = null;
        String sCurrentLine;
        String path="/home/webonise/.m2/repository/".concat("/").concat(artifact).concat("/").concat(artifact).concat("/").concat(version).concat("/").concat(artifact).concat("-").concat(version).concat(".pom");
        try {
			br = new BufferedReader(new FileReader(path));
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}