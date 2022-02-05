/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.cli.build;

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.renjin.RenjinVersion;
import org.renjin.eval.Session;
import org.renjin.packaging.BuildException;
import org.renjin.packaging.PackageDescription;
import org.renjin.packaging.PackageSource;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.io.Files;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Constructs a perfunctory Maven pom file containing only dependency information, not build instructions.
 */
public class PomBuilder {

  private static final Set<String> IGNORED_PACKAGES = Sets.newHashSet("R", "base");

  private final PackageBuild build;
  private final PackageSource source;
  private String buildVersion;
  private final PackageDescription description;
  private boolean includeBuild = false;

  public PomBuilder(PackageBuild build) {
    this.build = build;
    this.source = build.getSource();
    this.buildVersion = build.getBuildVersion();
    this.description = build.getSource().getDescription();
  }


  public File writePomFile() {

    File pomFile = new File(build.getMavenMetaDir(), "pom.xml");

    writePomFile(pomFile);

    return pomFile;
  }

  public void writePomFile(File pomFile) {
    try {
      Files.write(getXml(), pomFile, Charsets.UTF_8);
    } catch (IOException e) {
      throw new BuildException("Exception writing "   + pomFile.getAbsolutePath());
    }
  }

  public File writePomProperties() {

    File propertiesFile = new File(build.getMavenMetaDir(), "pom.properties");

    Properties properties = new Properties();
    properties.setProperty("groupId", source.getGroupId());
    properties.setProperty("artifactId", source.getPackageName());
    properties.setProperty("version", buildVersion);

    try {
      OutputStream out = new FileOutputStream(propertiesFile);
      properties.store(out, "Generated by Renjin");
    } catch (IOException e) {
      throw new BuildException("Exception writing " + propertiesFile.getAbsolutePath());
    }

    return propertiesFile;
  }

  public void setIncludeBuild(boolean includeBuild) {
    this.includeBuild = includeBuild;
  }

  private Model buildPom() {

    Model model = new Model();
    model.setModelVersion("4.0.0");
    model.setArtifactId(description.getPackage());
    model.setGroupId(source.getGroupId());
    model.setVersion(buildVersion);
    model.setDescription(description.getDescription());
    model.setUrl(description.getUrl());

    if(!Strings.isNullOrEmpty(description.getLicense())) {
      License license = new License();
      license.setName(description.getLicense());
      model.addLicense(license);
    }

    for(PackageDescription.Person author : description.getAuthors()) {
      Developer developer = new Developer();
      developer.setName(author.getName());
      developer.setEmail(author.getEmail());
      model.addDeveloper(developer);
    }

    for (org.eclipse.aether.graph.Dependency dependency : build.getDependencyResolution().getDependencies()) {
      Dependency mavenDep = new Dependency();
      mavenDep.setGroupId(dependency.getArtifact().getGroupId());
      mavenDep.setArtifactId(dependency.getArtifact().getArtifactId());
      mavenDep.setVersion(dependency.getArtifact().getVersion());
      model.addDependency(mavenDep);
    }

    DeploymentRepository deploymentRepo = new DeploymentRepository();
    deploymentRepo.setId("renjin-packages");
    deploymentRepo.setUrl("https://nexus.bedatadriven.com/content/repositories/renjin-packages");
    deploymentRepo.setName("Renjin CI Repository");

    DistributionManagement distributionManagement = new DistributionManagement();
    distributionManagement.setRepository(deploymentRepo);

    Repository bddRepo = new Repository();
    bddRepo.setId("bedatadriven-public");
    bddRepo.setUrl("https://nexus.bedatadriven.com/content/groups/public/");

    model.setDistributionManagement(distributionManagement);
    model.setRepositories(Lists.newArrayList(bddRepo));

    if(includeBuild) {

      Plugin renjinPlugin = new Plugin();
      renjinPlugin.setGroupId("org.renjin");
      renjinPlugin.setArtifactId("renjin-maven-plugin");
      renjinPlugin.setVersion(RenjinVersion.getVersionName());

      Xpp3Dom makeStrategy = new Xpp3Dom("make");
      makeStrategy.setValue("VAGRANT");

      Xpp3Dom executionConfiguration = new Xpp3Dom("configuration");
      executionConfiguration.addChild(makeStrategy);

      PluginExecution buildExecution = new PluginExecution();
      buildExecution.setId("build-package");
      buildExecution.setGoals(Collections.singletonList("gnur-compile"));
      buildExecution.setPhase("compile");
      buildExecution.setConfiguration(executionConfiguration);
      renjinPlugin.addExecution(buildExecution);

      PluginExecution testExecution = new PluginExecution();
      testExecution.setId("test-package");
      testExecution.setGoals(Collections.singletonList("test"));
      testExecution.setPhase("test");
      renjinPlugin.addExecution(testExecution);

      Build build = new Build();
      build.addPlugin(renjinPlugin);
      model.setBuild(build);
    }

    return model;
  }

  private boolean isPartOfRenjin(String dependencyName) {
    return NamespaceRegistry.CORE_PACKAGES.contains(dependencyName);
  }

  private Set<String> dependencies() {
    Set<String> included = new HashSet<>();

    // Add the "default" packages which are always meant to be on the search path
    included.addAll(Session.DEFAULT_PACKAGES);

    // Add the "tools" package which is often referenced by the if directive in the NAMESPACE file
    included.add("tools");

    // Add the packages specified in the Imports and Depends fields of the DESCRIPTION file
    for (PackageDescription.PackageDependency packageDep : Iterables.concat(description.getDepends(), description.getImports())) {
      included.add(packageDep.getName());
    }
    return included;
  }

  private void addCoreModule(Model model, String name) {
    Dependency mavenDep = new Dependency();
    mavenDep.setGroupId("org.renjin");
    mavenDep.setArtifactId(name);
    mavenDep.setVersion(RenjinVersion.getVersionName());
    model.addDependency(mavenDep);
  }

  public String getXml() throws IOException {
    Model pom = buildPom();
    StringWriter fileWriter = new StringWriter();
    MavenXpp3Writer writer = new MavenXpp3Writer();
    writer.write(fileWriter, pom);
    fileWriter.close();
    return fileWriter.toString();

  }
}
