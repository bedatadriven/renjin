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
package org.renjin.aether;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.version.Version;
import org.renjin.primitives.packaging.*;
import org.renjin.primitives.packaging.Package;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.*;

/**
 * A {@link PackageLoader} implementation that attempts to load packages from remote repositories.
 *
 * <p>When a package is requested, {@code AetherPackageLoader} first attempts to load the package from the given
 * {@code parentClassLoader}. If this is not successful, then the loader will query the remove
 */
public class AetherPackageLoader implements PackageLoader {

  private DynamicURLClassLoader classLoader;
  private ClasspathPackageLoader classpathPackageLoader;
  private final List<RemoteRepository> repositories = Lists.newArrayList();
  private final RepositorySystem system = AetherFactory.newRepositorySystem();
  private final DefaultRepositorySystemSession session = AetherFactory.newRepositorySystemSession(system);

  private PackageListener packageListener = null;


  /**
   * Keeps track of already-loaded packages. Each entry should be in the form groupId:artifactId
   */
  private Set<String> loadedPackages = new HashSet<>();

  /**
   *
   * @return the default remote repositories, which include Maven Central and Renjin's repository hosted by
   * BeDataDriven.
   */
  public static List<RemoteRepository> defaultRepositories() {
    return Arrays.asList(
        AetherFactory.mavenCentral(),
        AetherFactory.renjinRepo());
  }

  /**
   * Create a new {@code AetherPackageLoader} using this class' {@code ClassLoader} and
   * the {@link #defaultRepositories()}
   */
  public AetherPackageLoader() {
    this(AetherPackageLoader.class.getClassLoader(), defaultRepositories());
  }

  /**
   * Create a new {@code AetherPackageLoader} using the given {@code parentClassLoader} and the {@link #defaultRepositories()}
   *
   * @param parentClassLoader a {@link ClassLoader} that is first consulted when loading new packages before querying
   *                          remote repositories.
   */
  public AetherPackageLoader(ClassLoader parentClassLoader) {
    this(parentClassLoader, defaultRepositories());
  }

  /**
   * Create a new {@code AetherPackageLoader} using the given parent {@link ClassLoader} and list of
   * {@link RemoteRepository}
   *
   * @param parentClassLoader a {@link ClassLoader} that is first consulted when loading new packages before querying
   *                          remote repositories.
   */
  public AetherPackageLoader(ClassLoader parentClassLoader, List<RemoteRepository> remoteRepositories) {
    
    // Create our own ClassLoader to which we can add additional packages at runtime
    this.classLoader = new DynamicURLClassLoader(parentClassLoader);
    classpathPackageLoader = new ClasspathPackageLoader(this.classLoader);

    repositories.addAll(remoteRepositories);

    // Ensure that we don't load old versions of renjin onto the classpath
    // that might conflict with the current version.
    loadedPackages.add("org.renjin:renjin-core");
    loadedPackages.add("org.renjin:renjin-appl");
    loadedPackages.add("org.renjin:renjin-gnur-runtime");
    for (String corePackage : NamespaceRegistry.CORE_PACKAGES) {
      loadedPackages.add("org.renjin:" + corePackage);
    }
  }

  @Override
  public Optional<Package> load(FqPackageName name) {
    Optional<Package> pkg = classpathPackageLoader.load(name);
    if (pkg.isPresent()) {
      FqPackageName packageName = pkg.get().getName();
      loadedPackages.add(packageName.getGroupId() + ":" + packageName.getPackageName());
      return pkg;
    }
    try {
      
      if(packageListener != null) {
        packageListener.packageLoading(name);
      }

      Artifact latestArtifact = resolveLatestArtifact(name);

      if (latestArtifact == null) {
        if (packageListener != null) {
          packageListener.packageVersionResolutionFailed(name);
        }
        return Optional.empty();
      }

      if(packageListener != null) {
        packageListener.packageResolved(name, latestArtifact.getVersion());
      }
      
      CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRoot(new Dependency(latestArtifact, null));
      collectRequest.setRepositories(repositories);
      
      DependencyNode node = system.collectDependencies(session, collectRequest).getRoot();
      
      DependencyRequest dependencyRequest = new DependencyRequest();
      dependencyRequest.setRoot(node);
      dependencyRequest.setFilter(new AetherExclusionFilter(loadedPackages));
      dependencyRequest.setCollectRequest(collectRequest);
      
      DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

      for (ArtifactResult dependency : dependencyResult.getArtifactResults()) { 
        Artifact artifact = dependency.getArtifact();
        loadedPackages.add(artifact.getGroupId() + ":" + artifact.getArtifactId());
        classLoader.addArtifact(dependency);
      }

      if(packageListener != null) {
        packageListener.packageLoadSucceeded(name, latestArtifact.getVersion());
      }
      return classpathPackageLoader.load(name);
      
    } catch (DependencyResolutionException e) {
      if(packageListener != null) {
        packageListener.packageResolveFailed(e);
      }
      return Optional.empty();
      
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  private Artifact resolveLatestArtifact(FqPackageName name) throws VersionRangeResolutionException {
    Artifact artifact = new DefaultArtifact(name.getGroupId(), name.getPackageName(), "jar", "[0,)");
    Version newestVersion = resolveLatestVersion(artifact);
    if (newestVersion == null) {
      return null;
    }
    return artifact.setVersion(newestVersion.toString());
  }

  private Version resolveLatestVersion(Artifact artifact) throws VersionRangeResolutionException {
    VersionRangeRequest rangeRequest = new VersionRangeRequest();
    rangeRequest.setArtifact(artifact);
    rangeRequest.setRepositories(repositories);

    VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

    return rangeResult.getHighestVersion();
  }

  public void setTransferListener(TransferListener listener) {
    session.setTransferListener(listener);
  }
  
  public void setRepositoryListener(RepositoryListener listener) {
    session.setRepositoryListener(listener);
  }
  
  public void setPackageListener(PackageListener listener) {
    this.packageListener = listener;
  }

  public DynamicURLClassLoader getClassLoader() {
    return classLoader;
  }
}
