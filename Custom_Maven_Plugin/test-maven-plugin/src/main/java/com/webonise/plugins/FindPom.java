package com.webonise.plugins;

import java.io.File;
import java.io.FileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class FindPom extends AbstractMojo {

	// accepting .m2 path, opening the repository, storing all names of
	// sub-folders in a file array
	public void findRepository(String repository_path,
			String current_project_artifact, String current_project_version) {

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
				this.listf(each_file, current_project_artifact,
						current_project_version);
			} else {

			}
		}

	}

	// scanning the sub-folders and finding .pom file
	public void listf(File sub_directory, String current_project_artifact,
			String current_project_version) {
		File[] fList = sub_directory.listFiles();
		for (File each_file : fList) {
			if (each_file.isFile()) {
				if (each_file.getName().endsWith(".pom")) {
					PomContents pomContents = new PomContents();
					pomContents.getPomObject(each_file,
							current_project_artifact, current_project_version);
				}
			} else if (each_file.isDirectory()) {
				listf(each_file, current_project_artifact,
						current_project_version);
			}
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub

	}
}
