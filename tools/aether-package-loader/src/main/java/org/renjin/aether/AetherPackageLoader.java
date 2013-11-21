package org.renjin.aether;

import java.awt.geom.Path2D;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.crypto.DefaultSettingsDecrypter;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
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
  private static Settings settings;

  private static final SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();

  private static final SettingsDecrypter settingsDecrypter = new MavenSettingsDecrypter();
  
  private static final Logger LOGGER = Logger.getLogger(AetherPackageLoader.class.getName());

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
    return artifact.setVersion(newestVersion.toString());
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
    
    System.out.println("Using local repository: " + getLocalRepositoryDir());
    
    LocalRepository localRepo = new LocalRepository(getLocalRepositoryDir());
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

    session.setTransferListener(new ConsoleTransferListener());
    session.setRepositoryListener(new ConsoleRepositoryListener());

    // uncomment to generate dirty trees
    // session.setDependencyGraphTransformer( null );

    return session;
  }

  private static File getLocalRepositoryDir() {
    Settings settings = getSettings();
    if ( settings.getLocalRepository() != null )
    {
      return new File( settings.getLocalRepository() );
    }

    return new File( getMavenUserHome(), "repository" );
  }

  public static File getUserSettings()
  {
    return new File(getMavenUserHome(), "settings.xml" );
  }

  private static File getMavenUserHome() {
    return new File( getUserHome(), ".m2" );
  }

  private static File getUserHome() {
    return new File( System.getProperty( "user.home" ) );
  }

  private static File findGlobalSettings() {
    File mavenHome = getMavenHome();
    if ( mavenHome != null )
    {
      return new File( new File( mavenHome, "conf" ), "settings.xml" );
    }
    return null;
  }

  private static File getMavenHome() {
    if(!Strings.isNullOrEmpty(System.getenv("M2_HOME"))) {
      return new File(System.getenv("M2_HOME"));
    }
    String paths[] = Strings.nullToEmpty(System.getenv("PATH")).split(File.pathSeparator);
    for(String path : paths) {
      File pathDir = new File(path);
      if(pathDir.isDirectory()) {
        File bin = new File(pathDir, "bin");
        if(new File(bin, "mvn").exists() || new File(bin, "mvn.bat").exists()) {
          return pathDir;
        }
      }
    }
    return null;
  }

  private static synchronized Settings getSettings() {
    if ( settings == null ) {
      DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
      request.setUserSettingsFile(getUserSettings());
      File globalSettings = findGlobalSettings();
      if(globalSettings != null) {
        request.setGlobalSettingsFile(globalSettings);
      }

      try
      {
        settings = settingsBuilder.build( request ).getEffectiveSettings();
      }
      catch ( SettingsBuildingException e )
      {
        LOGGER.warning("Could not process settings.xml: " + e.getMessage());
      }

      SettingsDecryptionResult result =
              settingsDecrypter.decrypt( new DefaultSettingsDecryptionRequest( settings ) );
      settings.setServers( result.getServers() );
      settings.setProxies( result.getProxies() );
    }
    return settings;
  }
}
