	package com.webonise.plugins;

import java.io.File;
import java.io.FileFilter;
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
	
	/**Method finds all the .pom files in the directory 
	 * recursively in every sub-directory
	 * 
	 * @param directoryFile
	 */
	public void findPomModels(File directoryFile)
	{
		VersionResolver pomContents = new VersionResolver();
		
		//filter returning all the pom files in the present directory
		FileFilter pomFileFilter = new FileFilter() {
			public boolean accept(File file) {
				
				return (file.isFile()&&file.getName().endsWith(".pom"));
			}
		};
		
		if(directoryFile.listFiles(pomFileFilter).length!=0)
		{
		for (File currentFile : directoryFile.listFiles(pomFileFilter))
		{
			try
			{
				this.model = this.xmlReader.read(new FileReader(currentFile));
				this.model.setPomFile(currentFile);
				this.model.getUrl();
				pomContents.resolveDependencyVersion(this.model);
			}
			catch (XmlPullParserException e)
			{
				// skipping incompatible .pom files....
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		}
		
		//filter returning all the sub-directories in the passed directory
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		
		//calling the same method recursively for sub-directories
		for (File currentFile : directoryFile.listFiles(directoryFilter))
		{
				findPomModels(currentFile);
		}
	}
}
