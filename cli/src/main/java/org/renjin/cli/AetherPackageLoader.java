package org.renjin.cli;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.renjin.primitives.packaging.ClasspathPackage;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.Package;
import org.renjin.primitives.packaging.PackageLoader;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResolutionException;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.version.Version;

public class AetherPackageLoader implements PackageLoader {

  private RepositorySystem repoSystem;
  private ClasspathPackageLoader classpathLoader = new ClasspathPackageLoader();

  public AetherPackageLoader() throws Exception {
    repoSystem = newRepositorySystem();
  }
  
  private static RepositorySystem newRepositorySystem()
      throws Exception {
      return new DefaultPlexusContainer().lookup( RepositorySystem.class );
  }
  
  private static RepositorySystemSession newSession( RepositorySystem system ) {
      MavenRepositorySystemSession session = new MavenRepositorySystemSession();

      LocalRepository localRepo = new LocalRepository( "target/local-repo" );
      session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );

      return session;
  }
  
  
  @Override
  public Package load(String name) {
    ClasspathPackage pkg = classpathLoader.load(name);
    if(pkg != null) {
      return pkg;
    }
    try {
      return loadFromAether(name);
    } catch (VersionRangeResolutionException e) {
      e.printStackTrace();
      return null;
    }
  }

  private Package loadFromAether(String name) throws VersionRangeResolutionException {
    Version version = getLatestVersion(name);
    System.out.println(version);
    return null;
  }
  
  
  public Version getLatestVersion(String name) throws VersionRangeResolutionException {

    RepositorySystemSession session = newSession(repoSystem);
    
    Artifact artifact = new DefaultArtifact("org.renjin.cran:" + name +  ":[0,)" );

    VersionRangeRequest rangeRequest = new VersionRangeRequest();
    rangeRequest.setArtifact( artifact );
    rangeRequest.addRepository( new RemoteRepository("bdd", "default", "http://nexus.bedatadriven.com/content/repositories/oss-snapshots/" ) );

    VersionRangeResult rangeResult = repoSystem.resolveVersionRange( session, rangeRequest );

    return rangeResult.getHighestVersion();
  }
  
}
