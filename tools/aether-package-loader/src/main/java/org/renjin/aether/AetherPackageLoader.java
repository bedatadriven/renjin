package org.renjin.aether;

import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.file.FileRepositoryConnectorFactory;
import org.eclipse.aether.connector.wagon.WagonProvider;
import org.eclipse.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.version.Version;
import org.renjin.primitives.packaging.ClasspathPackage;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.Package;
import org.renjin.primitives.packaging.PackageLoader;

import com.google.common.collect.Lists;


public class AetherPackageLoader implements PackageLoader {

  private ClasspathPackageLoader classpathPackageLoader = new ClasspathPackageLoader();
  private final List<RemoteRepository> repositories = Lists.newArrayList();
  private final RepositorySystem system = newRepositorySystem();
  private final RepositorySystemSession session = newRepositorySystemSession(system);

  public AetherPackageLoader() {
    repositories.add(new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2/").build());
    repositories.add(new RemoteRepository.Builder("renjin", "default", "http://nexus.bedatadriven.com/content/groups/public/").build());
  }

  @Override
  public Package load(String name) {
    ClasspathPackage pkg = classpathPackageLoader.load(name);
    if (pkg != null) {
      return pkg;
    }
    try {

      Artifact latestArtifact = resolveLatestArtifact(name);

      if (latestArtifact == null) {
        return null;
      }

      ArtifactRequest collectRequest = new ArtifactRequest();
      collectRequest.setArtifact(latestArtifact);
      collectRequest.setRepositories(repositories);

      ArtifactResult artifactResult = system.resolveArtifact(session, collectRequest);

      System.out.println(artifactResult.getArtifact() + " resolved to " + artifactResult.getArtifact().getFile());

      return new AetherPackage(artifactResult.getArtifact());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Artifact resolveLatestArtifact(String name)
          throws VersionRangeResolutionException {
    Artifact artifact = new DefaultArtifact("org.renjin.cran:" + name + ":[0,)");
    Version newestVersion = resolveLatestVersion(artifact);
    if (newestVersion == null) {
      return null;
    }
    Artifact latestArtifact = artifact.setVersion(newestVersion.toString());
    return latestArtifact;
  }

  private Version resolveLatestVersion(Artifact artifact)
          throws VersionRangeResolutionException {
    VersionRangeRequest rangeRequest = new VersionRangeRequest();
    rangeRequest.setArtifact(artifact);
    rangeRequest.setRepositories(repositories);

    VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

    Version newestVersion = rangeResult.getHighestVersion();

    System.out.println("Newest version " + newestVersion + " from repository "
            + rangeResult.getRepository(newestVersion));
    return newestVersion;
  }

  public static RepositorySystem newRepositorySystem() {
    /*
    * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
    * prepopulated DefaultServiceLocator, we only need to register the repository connector factories.
    */
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
    locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
    locator.setServices(WagonProvider.class, new ManualWagonProvider());

    return locator.getService(RepositorySystem.class);
  }

  public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

    LocalRepository localRepo = new LocalRepository("target/local-repo");
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

    session.setTransferListener(new ConsoleTransferListener());
    session.setRepositoryListener(new ConsoleRepositoryListener());

    // uncomment to generate dirty trees
    // session.setDependencyGraphTransformer( null );

    return session;
  }
}
