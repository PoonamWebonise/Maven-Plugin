package com.webonise.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**Class responsible for scanning sub-directories in M2 repository
 * and fetching .pom files
 *  
 */
public class PomFileFinder{

	/**accepting repository path, opening the repository, storing all names of
	 * sub-folders in a file array
	 * 
	 * @param repository
	 */
	public void findSubDirectories(File repository) {

		
		//filter returning files only when they're directories
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		
		for (File currentFile : repository.listFiles(directoryFilter)){
			if (currentFile.isDirectory())
			{
				this.listAllFiles(currentFile);
			}
		}
	}

	/**method scanning the sub-folders recursively and finding .pom file
	 * 
	 * @param subDirectory
	 */
	public void listAllFiles(File subDirectory) {
		VersionResolver pomContents = new VersionResolver();
		for (File eachFile : subDirectory.listFiles()) {
			if (eachFile.isFile())
			{
				if (eachFile.getName().endsWith(".pom"))
				{
					try {
						pomContents.resolveDependencyVersion(eachFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{
				listAllFiles(eachFile);
			}
		}
	}
}