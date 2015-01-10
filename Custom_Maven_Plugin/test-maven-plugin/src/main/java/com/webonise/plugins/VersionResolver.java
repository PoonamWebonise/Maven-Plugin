package com.webonise.plugins;

import java.util.Iterator;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class VersionResolver {
	public void resolveDependencyVersion( MavenProject project, Log defaultLog)
	{
		String currentProjectArtifact = project.getArtifactId();
		String currentProjectVersion = project.getVersion();
		
		Iterator<Dependency> dependencyIterator = project.getDependencies().iterator();
		
		while (dependencyIterator.hasNext()) {
			Dependency current = dependencyIterator.next();
			String dependencyArtifact = current.getArtifactId();
			String dependencyVersion = current.getVersion();
			
			//if the required dependency exists in the project
			if (dependencyArtifact.equals(currentProjectArtifact)) {
				
				defaultLog.info("Dependency Artifact ID: " + dependencyArtifact+ "\tVersion: " + dependencyVersion);
				
				//if the version mismatches then produce ERROR message in build
				if (!currentProjectVersion.equalsIgnoreCase(dependencyVersion))
					defaultLog.error("DEPENDENCY VERSION MISMATCH in one of the Dependants. Please update xml and retry.");
				else
					defaultLog.info("Current Version: " + currentProjectVersion + "Version found in pom: " + dependencyVersion);
			}
		}
	}
}
