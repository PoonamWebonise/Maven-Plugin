package com.webonise.plugins;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class PomFileFinder{

	// accepting .m2 path, opening the repository, storing all names of
	// sub-folders in a file array
	public void findSubDirectories(File repository, String artifactID, String version) {

		
		//filter returning files only when they're directories
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		
		for (File currentFile : repository.listFiles(directoryFilter)){
			this.scanAllFiles(currentFile, artifactID, version);
		}
	}

	// scanning the sub-folders and finding .pom file
	public void scanAllFiles(File subDirectory, String artifactID, String version) {
		List<File> allPomFiles = new ArrayList<File>();
		PomContents pomContents = new PomContents();
		for (File eachFile : subDirectory.listFiles()) {
			if (eachFile.isFile())
			{
				if (eachFile.getName().endsWith(".pom"))
				{
					pomContents.getPomObject(eachFile, artifactID,version);
					
				}
			}
			else
			{
				scanAllFiles(eachFile, artifactID, version);
			}
		}
		
	}
}