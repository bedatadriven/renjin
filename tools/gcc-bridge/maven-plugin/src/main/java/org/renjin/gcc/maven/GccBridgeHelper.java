/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.AttachedArtifact;
import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 *
 */
public class GccBridgeHelper {

  private MavenProject project;
  
  public static void archiveHeaders(Log log, MavenProject project, File... includeDirectories) throws MojoExecutionException {
    
    File outputDir = new File(project.getBuild().getDirectory());
    File archiveFile = new File(outputDir, project.getBuild().getFinalName() + "-headers.jar");

    ensureDirExists(outputDir);

    try (JarOutputStream output = new JarOutputStream(new FileOutputStream(archiveFile))) {
      for (File includeDirectory : includeDirectories) {
        if(includeDirectory.exists()) {
          log.info("Archiving headers from " + includeDirectory.getAbsolutePath());
          archiveFiles(output, includeDirectory, "");
        }
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to create headers archive", e);
    }

    Artifact artifact = new AttachedArtifact( project.getArtifact(), "jar", "headers",  new HeaderArtifactHandler());
    artifact.setFile( archiveFile );
    artifact.setResolved( true );

    project.addAttachedArtifact( artifact );
  }

  private static void archiveFiles(JarOutputStream output, File dir, String prefix) throws IOException {
    File[] includeFiles = dir.listFiles();
    if (includeFiles != null) {
      for (File includeFile : includeFiles) {
        if (includeFile.isDirectory()) {
          archiveFiles(output, includeFile, prefix + includeFile.getName() + "/");
        } else {
          JarEntry entry = new JarEntry(prefix + includeFile.getName());
          output.putNextEntry(entry);
          Files.copy(includeFile.toPath(), output);
        }
      }
    }
  }
  public static void unpackHeaders(Log log, File dir, List<Artifact> artifacts) throws MojoExecutionException {
    ensureDirExists(dir);

    for (Artifact compileArtifact : artifacts) {
      if("headers".equals(compileArtifact.getClassifier())) {
        extractHeaders(log, compileArtifact, dir);
      }
    }
  }

  public static void extractHeaders(Log log, Artifact artifact,  File unpackedIncludeDir) throws MojoExecutionException {
    if(artifact.getFile() == null) {
      throw new MojoExecutionException("Depedency " + artifact.getId() + " has not been resolved.");
    }
    try(JarInputStream in = new JarInputStream(new FileInputStream(artifact.getFile()))) {
      JarEntry entry;
      while((entry=in.getNextJarEntry()) != null) {
        if(!entry.isDirectory()) {
          File destFile = new File(unpackedIncludeDir + File.separator + entry.getName());

          log.debug("Unpacking " + entry.getName() + " to " + destFile);

          ensureDirExists(destFile.getParentFile());
          org.renjin.repackaged.guava.io.Files.asByteSink(destFile).writeFrom(in);
        }
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Exception unpacking headers", e);
    }
  }

  public static void ensureDirExists(File unpackDir) throws MojoExecutionException {
    if(!unpackDir.exists()) {
      boolean created = unpackDir.mkdirs();
      if(!created) {
        throw new MojoExecutionException("Failed to create include directory " + unpackDir.getAbsolutePath());
      }
    }
  }


  public static ClassLoader getLinkClassLoader(MavenProject project, Log log) throws MojoExecutionException  {
    try {
      log.debug("GCC-Bridge Link Classpath: ");

      List<URL> classpathURLs = Lists.newArrayList();
      classpathURLs.add( new File(project.getBuild().getOutputDirectory()).toURI().toURL() );

      for(Artifact artifact : (List<Artifact>)project.getCompileArtifacts()) {
        log.debug("  "  + artifact.getFile());

        classpathURLs.add(artifact.getFile().toURI().toURL());
      }

      ClassLoader parent = GccBridgeHelper.class.getClassLoader();
      
      return new URLClassLoader( classpathURLs.toArray( new URL[ classpathURLs.size() ] ), parent);
    
    } catch(MalformedURLException e) {
      throw new MojoExecutionException("Exception resolving classpath", e);
    }
  }
}
