package org.sonatype.mavenbook.plugins;

import java.util.Iterator;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Dependency;

/**
 * Echos an object string to the output screen.
 * @goal echo
 * @requiresProject true
 */
public class EchoMojo extends AbstractMojo
{
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
    */
MavenProject project;
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info("Ketan Saxena");
        getLog().info("Project Version: "+project.getVersion().toString());
        getLog().info("Project Dependencies: ");
		List<Dependency> dependencies = project.getDependencies();
        Iterator<Dependency> myIterator= dependencies.iterator();
        while(myIterator.hasNext())
        {
        	Dependency current = myIterator.next(); 
        	getLog().info("Artifact ID: "+current.getArtifactId()+"\tVersion: "+current.getVersion());
        }
    }
}