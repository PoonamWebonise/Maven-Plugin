package com.webonise.plugins;

import org.apache.maven.plugin.logging.Log;

public class DependencyVersionMismatchError extends Error {

public DependencyVersionMismatchError(Log defaultLog, int incompatiblePomFileCount) {
	defaultLog.info("skipped "+incompatiblePomFileCount + " incompatible pom files in your repository.");
	defaultLog.error("THE TARGET PROJECT HAS A MISMATCHED VERSION AS A DEPENDENCY IN ONE OF THE PROJECTS IN REPOSITORY");
}
}