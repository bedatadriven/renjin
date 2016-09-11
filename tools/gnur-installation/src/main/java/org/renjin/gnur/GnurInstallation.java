package org.renjin.gnur;


import org.renjin.repackaged.guava.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GnurInstallation {

  /**
   * Unpacks the R Home resources to the given {@code targetDir}.
   */
  public static File unpackRHome(File targetDir) throws IOException {

    URL url = GnurInstallation.class.getResource("include/R.h");

    if(url.getProtocol().equals("file")) {
      return new File(url.getFile()).getParentFile().getParentFile();

    } else if(url.getProtocol().equals("jar")) {
      // file = file:/C:/Users/Alex/.m2/repository/org/renjin/renjin-gnur-compiler/0.7.0-SNAPSHOT/renjin-gnur-compiler-0.7.0-SNAPSHOT.jar!/include/R.h
      if(url.getFile().startsWith("file:")) {

        int fileStart = url.getFile().indexOf("!");
        String jarPath = url.getFile().substring("file:".length(), fileStart);
        String includePath = url.getFile().substring(fileStart+1+"/".length());
        includePath = includePath.substring(0, includePath.length() - "include/R.h".length());

        return extractToTemp(jarPath, includePath, targetDir);
      }
    }
    throw new RuntimeException("Don't know how to unpack resources at "  + url);
  }

  private static File extractToTemp(String jarPath, String includePath, File targetDir) throws IOException {

    JarFile jar = new JarFile(jarPath);
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if (entry.getName().startsWith(includePath) && !entry.isDirectory()) {
        File target = new File(targetDir.getAbsolutePath() + File.separator +
            entry.getName().substring(includePath.length()).replace('/', File.separatorChar));

        ensureDirExists(target.getParentFile());

        InputStream in = jar.getInputStream(entry);
        FileOutputStream out = new FileOutputStream(target);
        ByteStreams.copy(in, out);
        out.close();
      }
    }
    return targetDir;
  }

  private static void ensureDirExists(File dir) throws IOException {
    if(!dir.exists()) {
      boolean created = dir.mkdirs();
      if(!created) {
        throw new IOException("Failed to create directory " + dir.getAbsolutePath());
      }
    }
  }

}
