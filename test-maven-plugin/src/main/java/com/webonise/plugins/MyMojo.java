package com.webonise.plugins;

import java.util.Iterator;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
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

    public void execute() throws MojoExecutionException {
        getLog().info("this is not coming");
        getLog().info("Project Version: " + project.getVersion().toString());
        getLog().info("Project Dependencies: ");
        List<Dependency> dependencies = project.getDependencies();
        Iterator<Dependency> myIterator = dependencies.iterator();
        while (myIterator.hasNext()) {
            Dependency current = myIterator.next();
            getLog().info(
                    "Artifact ID: " + current.getArtifactId() + "\tVersion: "
                            + current.getVersion());
        }
    }

}
