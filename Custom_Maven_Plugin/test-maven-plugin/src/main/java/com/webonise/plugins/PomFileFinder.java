	package com.webonise.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * class responsible to find Pom files from the local repository
 *
 *@author webonise
 */
public class PomFileFinder{


	/** Model object to represent project from a pom file*/
	private Model model;
	
	/** Object to parse the pom File and return a Model*/
	private MavenXpp3Reader xmlReader;
	
	/**
	 * default constructor initializing MavenXpp3Reader object
	 */
	public PomFileFinder()
	{
		this.xmlReader = new MavenXpp3Reader();
	}
	
	/**accepting .m2 path, opening the repository, storing all names of
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

	
	/**scanning the sub-folders and finding .pom file
	 * 
	 * @param subDirectory
	 */
	public void listAllFiles(File subDirectory) {
		VersionResolver pomContents = new VersionResolver();
		for (File eachFile : subDirectory.listFiles())
		{
			if (eachFile.isFile())
			{
				if (eachFile.getName().endsWith(".pom"))
				{
					try
					{
						this.model = this.xmlReader.read(new FileReader(eachFile));
						this.model.setPomFile(eachFile);
						pomContents.resolveDependencyVersion(this.model);
					}
					catch (XmlPullParserException e)
					{	
						//skipping incompatible .pom files....
					}
					catch (IOException e)
					{
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