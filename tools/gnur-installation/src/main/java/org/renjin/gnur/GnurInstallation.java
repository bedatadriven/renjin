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
package org.renjin.gnur;


import org.renjin.repackaged.guava.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
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

    File bin = new File(new File(targetDir, "bin"), "R");
    bin.setExecutable(true, false);

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
