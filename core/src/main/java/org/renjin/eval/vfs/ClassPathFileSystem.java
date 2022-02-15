package org.renjin.eval.vfs;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarFile;

class ClassPathFileSystem extends AbstractFileSystem {

  private ClassLoader classLoader;

  protected ClassPathFileSystem(ClassLoader classLoader, FileName rootName, FileSystemOptions fileSystemOptions) {
    super(rootName, null, fileSystemOptions);
    this.classLoader = classLoader;
  }


  @Override
  protected FileObject createFile(AbstractFileName name) throws Exception {

    // Remove initial '/'
    String resourcePath = name.getPath().substring(1);

    // Search the classpath for the resource
    Enumeration<URL> resource = classLoader.getResources(resourcePath);

    if(!resource.hasMoreElements()) {
      return new NonExistentClassPathFileObject(name, this);
    }

    // List matching files
    List<URL> urls = new ArrayList<>();

    while(resource.hasMoreElements()) {
      URL url = resource.nextElement();
      if ("file".equals(url.getProtocol())) {
        File file = new File(fileFromUrl(url));
        if (file.exists() && !file.isDirectory()) {
          return createFile(name, url);
        }
      } else if ("classpath".equals(url.getProtocol())) {
        return createFile(name, url);
      }
      urls.add(url);
    }

    // If it's a single URL, stop here
    if(urls.size() == 1) {
      return createFile(name, urls.get(0));
    }

    // Otherwise list the children
    List<File> localDirs = new ArrayList<>();
    Set<String> children = new HashSet<>();

    for (URL url : urls) {
      if ("file".equals(url.getProtocol())) {
        File file = new File(fileFromUrl(url));
        localDirs.add(file);
      } else if("jar".equals(url.getProtocol())) {
        listJarFiles(url, children);
      } else {
        throw new UnsupportedOperationException("TODO: " + url);
      }
    }
    return new CompositeClassDirectoryObject(name, this, localDirs, children);
  }

  private void listJarFiles(URL url, Set<String> children) throws IOException {
    String path = url.getPath();
    int sep = path.indexOf('!');
    if(path.startsWith("file:")) {
      String jarFilePath = path.substring("file:".length(), sep);
      String prefix = path.substring(sep + 2) + "/";

      JarFile file = new JarFile(jarFilePath);

      file.stream()
          .filter(e -> e.getName().startsWith(prefix) &&
                       e.getName().length() > prefix.length())
          .forEach(e -> {
            String name = e.getName();
            String next = name.substring(prefix.length());
            int folderEnd = next.indexOf('/');
            if (folderEnd == -1) {
              children.add(next);
            } else {
              children.add(next.substring(0, folderEnd));
            }
          });
    } else {
      throw new UnsupportedOperationException("TODO: cannot handle url: " + url);
    }
  }

  private FileObject createFile(AbstractFileName name, URL resource) {
    if(resource.getProtocol().equals("file")) {
      return new ClassPathFileObject(this, name, new File(fileFromUrl(resource)));
    } else {
      return new OpaqueClassFileObject(name, this, resource);
    }
  }

  static String fileFromUrl(URL resource) {
    try {
      return URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      // Will never happen unless this is a customized JVM
      throw new IllegalStateException("JVM does not support UTF-8 Encoding");
    }
  }

  @Override
  protected void addCapabilities(Collection<Capability> caps) {
    caps.addAll(ClassPathFileProvider2.CAPABILITIES);
  }

  @Override
  protected File doReplicateFile(FileObject file, FileSelector selector) throws Exception {
    if(file instanceof ClassPathFileObject) {
      return ((ClassPathFileObject) file).getFile();
    }
    return super.doReplicateFile(file, selector);
  }
}
