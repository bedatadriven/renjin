package org.renjin.maven;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.model.Build;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.renjin.maven.PackageDescription.Person;

import com.google.common.base.Strings;

/**
 * Builds a maven POM from a DESCRIPTION file
 *
 */
public class PackagePomBuilder {

	public static Model build(PackageDescription description) {
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setArtifactId(description.getPackage());
		model.setGroupId("org.r-project.cran");
		model.setVersion(description.getVersion() + "-SNAPSHOT");
		model.setDescription(description.getDescription());
		model.setUrl(description.getUrl());
		
		if(!Strings.isNullOrEmpty(description.getLicense())) {
			License license = new License();
			license.setName(description.getLicense());
			model.addLicense(license);
		}
		
		for(Person author : description.getAuthors()) {
			Developer developer = new Developer();
			developer.setName(author.getName());
			developer.setEmail(author.getEmail());
			model.addDeveloper(developer);
		}
		
		Plugin renjinPlugin = new Plugin();
		renjinPlugin.setGroupId("org.renjin");
		renjinPlugin.setArtifactId("renjin-maven-plugin");
		renjinPlugin.setVersion("0.6.8-SNAPSHOT");
		
		PluginExecution compileExecution = new PluginExecution();
		compileExecution.setId("renjin-compile");
		compileExecution.addGoal("compile");
	
		renjinPlugin.addExecution(compileExecution);
		
		Build build = new Build();
		build.addPlugin(renjinPlugin);
		
		model.setBuild(build);
		
		Repository bddRepo = new Repository();
		bddRepo.setId("bedatadriven-public");
		bddRepo.setUrl("http://nexus.bedatadriven.com/content/groups/public");
		bddRepo.setName("bedatadriven Public Repo");
		model.addRepository(bddRepo);
		
		
		return model;
	}
	
}
  