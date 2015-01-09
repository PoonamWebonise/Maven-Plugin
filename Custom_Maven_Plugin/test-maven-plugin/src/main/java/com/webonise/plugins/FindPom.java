package com.webonise.plugins;

import java.io.File;
import java.io.FileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class FindPom extends AbstractMojo {

	public void findRepository(String repository_path) {

		File repository_files_link = new File(repository_path);
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		File[] all_repository_files = repository_files_link
				.listFiles(directoryFilter);
		for (File each_file : all_repository_files) {
			if (each_file.isDirectory()) {
				this.listf(each_file);
			} else {

			}
		}

	}

	public void listf(File sub_directory) {
		File[] fList = sub_directory.listFiles();
		for (File each_file : fList) {
			if (each_file.isFile()) {
				if (each_file.getName().endsWith(".pom")) {
					getLog().info("pom file:" + each_file);
					PomContents pomContents = new PomContents();
					pomContents.getPomObject(each_file);
				}
			} else if (each_file.isDirectory()) {
				listf(each_file);
			}
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub

	}
}
