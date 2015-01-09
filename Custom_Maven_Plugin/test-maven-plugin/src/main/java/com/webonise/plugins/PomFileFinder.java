package com.webonise.plugins;

import java.io.File;
import java.io.FileFilter;

public class PomFileFinder{

	// accepting .m2 path, opening the repository, storing all names of
	// sub-folders in a file array
	public void findRepository(String repositoryPath, String currentProjectArtifact, String currentProjectVersion) {

		File repository = new File(repositoryPath);
		
		//filter returning files only when they're directories
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		
		for (File currentFile : repository.listFiles(directoryFilter)){
			if (currentFile.isDirectory())
			{
				this.listAllFiles(currentFile, currentProjectArtifact, currentProjectVersion);
			}
		}
	}

	// scanning the sub-folders and finding .pom file
	public void listAllFiles(File subDirectory, String currentProjectArtifact, String currentProjectVersion) {
		PomContents pomContents = new PomContents();
		for (File each_file : subDirectory.listFiles()) {
			if (each_file.isFile())
			{
				if (each_file.getName().endsWith(".pom"))
				{
					pomContents.getPomObject(each_file, currentProjectArtifact, currentProjectVersion);
				}
			}
			else
			{
				listAllFiles(each_file, currentProjectArtifact,currentProjectVersion);
			}
		}
	}
}