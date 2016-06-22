package org.renjin.aether;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
  private Set<String> loadedPackages = new HashSet<String>();

  public AetherPackageLoader() {
    
    // Create our own ClassLoader to which we can add additional packages at runtime
    classLoader = new DynamicURLClassLoader(getClass().getClassLoader());
    classpathPackageLoader = new ClasspathPackageLoader(classLoader);

    repositories.add(AetherFactory.mavenCentral());
    repositories.add(AetherFactory.renjinRepo());
    
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
        return Optional.absent();
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
      packageListener.packageResolveFailed(e);
      return Optional.absent();
      
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
