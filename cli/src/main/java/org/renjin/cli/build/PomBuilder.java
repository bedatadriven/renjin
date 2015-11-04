package org.renjin.cli.build;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.renjin.RenjinVersion;
import org.renjin.eval.Session;
import org.renjin.packaging.PackageDescription;
import org.renjin.primitives.packaging.NamespaceRegistry;

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
  private final PackageDescription description;

  public PomBuilder(PackageSource source) {
    this.source = source;
    this.description = source.getDescription();
  }

  private Model buildPom() throws IOException {

    Model model = new Model();
    model.setModelVersion("4.0.0");
    model.setArtifactId(description.getPackage());
    model.setGroupId(source.getGroupId());
    model.setVersion(source.getVersion());
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
