package com.webonise.plugins;

import org.apache.maven.plugin.logging.Log;

public class DependencyVersionMismatchError extends Error {

public DependencyVersionMismatchError(Log defaultLog) {
	defaultLog.error("THE TARGET PROJECT HAS A MISMATCHED VERSION AS A DEPENDENCY IN ONE OF THE PROJECTS IN REPOSITORY");
}
}