/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.renjin.repackaged.guava.base.Strings;

import java.io.File;
import java.util.logging.Logger;

/**
 * Creates and initializes the Aether RepositorySystem and RepositorySystemSession instances needed to 
 * resolve and deploy artifacts.
 */
public class AetherFactory {

  private static final SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
  private static final SettingsDecrypter settingsDecrypter = new MavenSettingsDecrypter();
  private static final Logger LOGGER = Logger.getLogger(AetherPackageLoader.class.getName());
  private static Settings settings;

  private AetherFactory() { }

  public static RepositorySystem newRepositorySystem() {
    /*
    * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
    * prepopulated DefaultServiceLocator, we only need to register the repository connector factories.
    */
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService( TransporterFactory.class, FileTransporterFactory.class );
    locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

    return locator.getService(RepositorySystem.class);
  }

  public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
    
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
    LocalRepository localRepo = new LocalRepository(getLocalRepositoryDir());
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
    
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
    return new File( System.getProperty("user.home") );
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
    String[] paths = Strings.nullToEmpty(System.getenv("PATH")).split(File.pathSeparator);
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

  /**
   * @return a {@link RemoteRepository} object for the Maven central repository
   */
  public static RemoteRepository mavenCentral() {
    return new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
  }

  /**
   * @return a {@link RemoteRepository} object for Renjin's primary public repository
   */
  public static RemoteRepository renjinRepo() {
    return new RemoteRepository.Builder("renjin", "default", "https://nexus.bedatadriven.com/content/groups/public/").build();
  }
}
