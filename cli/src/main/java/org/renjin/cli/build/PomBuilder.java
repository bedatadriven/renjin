/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import org.renjin.RenjinVersion;
import org.renjin.eval.Session;
import org.renjin.packaging.PackageDescription;
import org.renjin.packaging.PackageSource;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Constructs a perfunctory Maven pom file containing only dependency information, not build instructions.
 */
public class PomBuilder {

  private static final Set<String> IGNORED_PACKAGES = Sets.newHashSet("R", "base");

  private final PackageSource source;
  private String buildVersion;
  private final PackageDescription description;

  public PomBuilder(PackageSource source, String buildVersion) {
    this.source = source;
    this.buildVersion = buildVersion;
    this.description = source.getDescription();
  }

  private Model buildPom() throws IOException {

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

    for(String dependencyName : dependencies()) {
      if(!IGNORED_PACKAGES.contains(dependencyName)) {

        if(isPartOfRenjin(dependencyName)) {
          addCoreModule(model, dependencyName);

        } else {
          throw new UnsupportedOperationException(dependencyName);
        }
      }
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

    return model;
  }

  private boolean isPartOfRenjin(String dependencyName) {
    return NamespaceRegistry.CORE_PACKAGES.contains(dependencyName);
  }

  private Set<String> dependencies() {
    Set<String> included = new HashSet<String>();

    // Add the "default" packages which are always meant to be on the search path
    included.addAll(Session.DEFAULT_PACKAGES);

    // Add the "tools" package which is often referenced by the if() directive in the NAMESPACE file
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

  public String getXml()  {
    try {
      Model pom = buildPom();
      StringWriter fileWriter = new StringWriter();
      MavenXpp3Writer writer = new MavenXpp3Writer();
      writer.write(fileWriter, pom);
      fileWriter.close();
      return fileWriter.toString();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
