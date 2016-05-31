package org.renjin.maven;

import com.google.common.io.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.renjin.gcc.maven.GccBridgeHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Common methods for GNU R compilation
 */
public class GnurCompilation {
  
  
  public static File unpackIncludes(File targetDir) throws MojoExecutionException {

    URL url = Resources.getResource("R.h");
    if(url.getProtocol().equals("file")) {
      return new File(url.getFile()).getParentFile();
    } else if(url.getProtocol().equals("jar")) {
      // file = file:/C:/Users/Alex/.m2/repository/org/renjin/renjin-gnur-compiler/0.7.0-SNAPSHOT/renjin-gnur-compiler-0.7.0-SNAPSHOT.jar!/org/renjin/gnur/include/R.h
      if(url.getFile().startsWith("file:")) {

        int fileStart = url.getFile().indexOf("!");
        String jarPath = url.getFile().substring("file:".length(), fileStart);
        String includePath = url.getFile().substring(fileStart+1+"/".length());
        includePath = includePath.substring(0, includePath.length() - "R.h".length());

        return extractToTemp(jarPath, includePath, targetDir);
      }
    }
    throw new RuntimeException("Don't know how to unpack resources at "  + url);
  }

  private static File extractToTemp(String jarPath, String includePath, File targetDir) 
      throws MojoExecutionException {
    
    try {
      JarFile jar = new JarFile(jarPath);
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.getName().startsWith(includePath) && !entry.isDirectory()) {
          File target = new File(targetDir.getAbsolutePath() + File.separator +
              entry.getName().substring(includePath.length()).replace('/', File.separatorChar));

          GccBridgeHelper.ensureDirExists(target.getParentFile());

          //System.err.println("extracting to "  + target);

          InputStream in = jar.getInputStream(entry);
          FileOutputStream out = new FileOutputStream(target);
          ByteStreams.copy(in, out);
          out.close();
        }
      }
      return targetDir;
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to unpack GNU R headers", e);
    }
  }
  
  public static void extractResource(String resourceName, File target) throws MojoExecutionException {
    ByteSource source = Resources.asByteSource(Resources.getResource(GnurCompilation.class, resourceName));
    ByteSink dest = Files.asByteSink(target);
    try {
      source.copyTo(dest);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to extract " + resourceName, e);
    }
  }


}
