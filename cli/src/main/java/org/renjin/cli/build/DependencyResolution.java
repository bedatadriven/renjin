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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;

/**
 * Resolves a package's dependencies from a DESCRIPTION file.
 */
public class DependencyResolution {

  private final URLClassLoader classLoader;
  private final PackageLoader packageLoader;
  private DependencyNode node;
  private Map<String, ResolvedDependency> resolved;

  public DependencyResolution(CliBuildLogger logger, PackageDescription description) {

    logger.info("Resolving dependencies...");

    Iterable<PackageDescription.PackageDependency> dependencies = concat(
        description.getDepends(),
        description.getImports());

    PackageRepoClient repoClient = new PackageRepoClient();

    try {
      resolved = repoClient.resolve(dependencies);
    } catch (IOException e) {
      throw new BuildException("Querying packages.renjin.org failed.", e);
    }

    logger.info("Resolving transitive dependencies...");

    RepositorySystem system = AetherFactory.newRepositorySystem();
    DefaultRepositorySystemSession session = AetherFactory.newRepositorySystemSession(system);
    List<RemoteRepository> repositories = AetherPackageLoader.defaultRepositories();

    CollectRequest collectRequest = new CollectRequest();
    collectRequest.setRepositories(repositories);

    for (PackageDescription.PackageDependency depend : dependencies) {
      if(!isExcluded(depend)) {
        collectRequest.addDependency(qualify(resolved, depend));
      }
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

    classLoader = new URLClassLoader(urls.stream().toArray(URL[]::new));
    packageLoader = new ClasspathPackageLoader(classLoader);
  }

  public ResolvedDependency getResolvedDependency(String name) {
    return resolved.get(name);
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
}
