package org.renjin.cli.build;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.renjin.RenjinVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Creates a JAR from the package
 */
public class JarArchiver implements AutoCloseable {
  
  private static final Attributes.Name CREATED_BY = new Attributes.Name("Created-By");
  
  public JarOutputStream output;

  public JarArchiver(File jarFile) throws IOException {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(CREATED_BY, "Renjin " + RenjinVersion.getVersionName());

    this.output = new JarOutputStream(new FileOutputStream(jarFile), manifest);
  }

  public void addDirectory(File dir) throws IOException {
    add(dir, dir);
  }
  
  private void add(File relativeTo, File source) throws IOException {
    JarEntry entry = new JarEntry(relative(relativeTo, source));
    entry.setTime(source.lastModified());
    
    output.putNextEntry(entry);

    if(source.isFile()) {
      Files.copy(source, output);
    }
    
    output.closeEntry();
    
    if(source.isDirectory()) {
      File[] children = source.listFiles();
      if(children != null) {
        for (File child : children) {
          add(relativeTo, child);
        }
      }
    }
  }
  
  public void addFile(File source, String fileNameInJar) throws IOException {
    Preconditions.checkArgument(source.isFile());
    
    JarEntry entry = new JarEntry(fileNameInJar);
    entry.setTime(source.lastModified());

    output.putNextEntry(entry);
    
    Files.copy(source, output);
  }

  private String relative(File relativeTo, File source) {
    String rootPath = relativeTo.getAbsolutePath().replace('\\', '/');
    String nestedPath = source.getAbsolutePath().replace('\\', '/');
    if(!nestedPath.startsWith(rootPath)) {
      throw new IllegalStateException(String.format("'%s' is not a child of '%s'", nestedPath, rootPath));
    }
    String path = nestedPath.substring(rootPath.length());
    while(path.startsWith("/")) {
      path = path.substring(1);
    }
    if(source.isDirectory() && !path.endsWith("/")) {
      path += "/";
    }
    return path;
  }

  @Override
  public void close() throws IOException {
    output.close();
  }
}
