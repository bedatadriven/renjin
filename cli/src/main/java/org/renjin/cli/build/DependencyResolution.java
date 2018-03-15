/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
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
import org.renjin.packaging.PackageDescription;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.primitives.packaging.PackageLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.concat;

/**
 * Resolves a package's dependencies from a DESCRIPTION file.
 */
public class DependencyResolution {

  private final URLClassLoader classLoader;
  private final PackageLoader packageLoader;

  public DependencyResolution(PackageDescription description) throws DependencyCollectionException, DependencyResolutionException, MalformedURLException {

    RepositorySystem system = AetherFactory.newRepositorySystem();
    DefaultRepositorySystemSession session = AetherFactory.newRepositorySystemSession(system);
    List<RemoteRepository> repositories = AetherPackageLoader.defaultRepositories();

    CollectRequest collectRequest = new CollectRequest();
    collectRequest.setRepositories(repositories);

    for (PackageDescription.PackageDependency depend : concat(
        description.getDepends(),
        description.getImports())) {

      if(!isExcluded(depend)) {
        collectRequest.addDependency(qualify(depend));
      }
    }

    DependencyNode node = system.collectDependencies(session, collectRequest).getRoot();

    DependencyRequest dependencyRequest = new DependencyRequest();
    dependencyRequest.setRoot(node);
//    dependencyRequest.setFilter(new ClasspathExclusionFilter());
    dependencyRequest.setCollectRequest(collectRequest);

    DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

    List<URL> urls = new ArrayList<>();
    for (ArtifactResult result : dependencyResult.getArtifactResults()) {
      urls.add(result.getArtifact().getFile().toURI().toURL());
    }

    classLoader = new URLClassLoader(urls.stream().toArray(URL[]::new));
    packageLoader = new ClasspathPackageLoader(classLoader);

  }

  private boolean isExcluded(PackageDescription.PackageDependency depend) {
    if(depend.getName().equals("R")) {
      return true;
    }
    if(NamespaceRegistry.CORE_PACKAGES.contains(depend.getName())) {
      return true;
    }
    return false;
  }

  private Dependency qualify(PackageDescription.PackageDependency dependency) {
    Artifact artifact = new DefaultArtifact("org.renjin.cran", dependency.getName(), "jar",
        dependency.getVersionRange());
    return new Dependency(artifact, "compile");
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public PackageLoader getPackageLoader() {
    return packageLoader;
  }
}
