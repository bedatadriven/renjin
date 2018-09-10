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

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.renjin.aether.AetherFactory;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.packaging.BuildException;
import org.renjin.packaging.PackageDescription;
import org.renjin.packaging.PackageRepoClient;
import org.renjin.packaging.ResolvedDependency;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.primitives.packaging.PackageLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static org.renjin.repackaged.guava.collect.Iterables.concat;


/**
 * Resolves a package's dependencies from a DESCRIPTION file.
 */
public class DependencyResolution {

  private final URLClassLoader classLoader;
  private final PackageLoader packageLoader;
  private DependencyNode node;
  private List<Dependency> dependencies;

  public DependencyResolution(CliBuildLogger logger, PackageDescription description) {

    logger.info("Resolving dependencies...");

    if(description.hasProperty("Dependencies")) {
      dependencies = resolveQualifiedDependencies(description);
    } else {
      dependencies = resolveUnqualifiedDependencies(description);
    }

    logger.info("Resolving transitive dependencies...");

    RepositorySystem system = AetherFactory.newRepositorySystem();
    DefaultRepositorySystemSession session = AetherFactory.newRepositorySystemSession(system);
    List<RemoteRepository> repositories = AetherPackageLoader.defaultRepositories();

    CollectRequest collectRequest = new CollectRequest();
    collectRequest.setRepositories(repositories);

    for (Dependency dependency : dependencies) {
      collectRequest.addDependency(dependency);
    }

    logger.info("Downloading dependencies...");

    try {
      node = system.collectDependencies(session, collectRequest).getRoot();
    } catch (DependencyCollectionException e) {
      throw new BuildException("Dependency collection failed.", e);
    }

    DependencyRequest dependencyRequest = new DependencyRequest();
    dependencyRequest.setRoot(node);
    dependencyRequest.setCollectRequest(collectRequest);

    DependencyResult dependencyResult = null;
    try {
      dependencyResult = system.resolveDependencies(session, dependencyRequest);
    } catch (DependencyResolutionException e) {
      throw new BuildException("Failed to retrieve dependencies.", e);
    }

    List<URL> urls = new ArrayList<>();
    for (ArtifactResult result : dependencyResult.getArtifactResults()) {
      try {
        urls.add(result.getArtifact().getFile().toURI().toURL());
      } catch (MalformedURLException e) {
        throw new BuildException("Malformed artifact URL", e);
      }
    }

    classLoader = new URLClassLoader(urls.toArray(new URL[0]));
    packageLoader = new ClasspathPackageLoader(classLoader);
  }

  private List<Dependency> resolveQualifiedDependencies(PackageDescription description) {

    List<Dependency> dependencies = new ArrayList<>();
    for (PackageDescription.Dependency dependency : description.getDependencyList()) {
      Artifact artifact = new DefaultArtifact(
          dependency.getGroupId(),
          dependency.getName(), "jar",
          dependency.getVersion());

      dependencies.add(new Dependency(artifact, "compile"));
    }
    return dependencies;
  }

  private List<Dependency> resolveUnqualifiedDependencies(PackageDescription description) {

    Iterable<PackageDescription.PackageDependency> dependencies = concat(
        description.getDepends(),
        description.getImports());


    Set<String> packageNames = new HashSet<>();
    for (PackageDescription.PackageDependency dependency : dependencies) {
      packageNames.add(dependency.getName());
    }

    Map<String, ResolvedDependency> resolved;
    try {
      PackageRepoClient repoClient = new PackageRepoClient();
      resolved = repoClient.resolve(packageNames);
    } catch (IOException e) {
      throw new BuildException("Querying packages.renjin.org failed.", e);
    }

    List<Dependency> dependencyList = new ArrayList<>();

    for (PackageDescription.PackageDependency depend : dependencies) {
      if(!isExcluded(depend)) {
        dependencyList.add(qualify(resolved, depend));
      }
    }

    return dependencyList;
  }

  public List<Dependency> getDependencies() {
    return dependencies;
  }

  private boolean isExcluded(PackageDescription.PackageDependency depend) {
    return depend.getName().equals("R") ||
            NamespaceRegistry.CORE_PACKAGES.contains(depend.getName());
  }

  private Dependency qualify(Map<String, ResolvedDependency> resolved, PackageDescription.PackageDependency dependency) {

    ResolvedDependency resolvedDependency = resolved.get(dependency.getName());
    if(resolvedDependency == null) {
      throw new BuildException("Failed to resolve " + dependency.getName());
    }

    Artifact artifact = new DefaultArtifact(
        resolvedDependency.getGroupId(),
        dependency.getName(), "jar",
        resolvedDependency.getVersion());

    return new Dependency(artifact, "compile");
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public PackageLoader getPackageLoader() {
    return packageLoader;
  }

  public Map<String, String> getPackageGroupMap() {
    Map<String, String> map = new HashMap<>();
    for (Dependency dependency : dependencies) {
      map.put(dependency.getArtifact().getArtifactId(), dependency.getArtifact().getGroupId());
    }
    return map;
  }
}
