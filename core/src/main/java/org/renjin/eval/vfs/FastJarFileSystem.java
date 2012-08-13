package org.renjin.eval.vfs;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.zip.ZipFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class FastJarFileSystem extends AbstractFileSystem
implements FileSystem {

  private final static Log log = LogFactory.getLog(ZipFileSystem.class);

  private final File file;
  private boolean exists;
  private JarFile jarFile;
  private NonExistentJarFileObject nonExistentJarFileObject;

  public FastJarFileSystem(final FileName rootName,
      final FileObject parentLayer,
      final FileSystemOptions fileSystemOptions)
          throws FileSystemException {
    super(rootName, parentLayer, fileSystemOptions);

    // Make a local copy of the file
    file = parentLayer.getFileSystem().replicateFile(parentLayer, Selectors.SELECT_SELF);

    // Open the Zip file
    this.exists = file.exists();
    if (file.exists()) {
      try {
        jarFile = new JarFile(file);
      } catch (IOException e) {
        throw new FileSystemException(e);
      }
    }
  }


  @Override
  protected void doCloseCommunicationLink() {
    // Release the zip file
    try
    {
      if (jarFile != null)
      {
        jarFile.close();
        jarFile = null;
      }
    }
    catch (final IOException e)
    {
      // getLogger().warn("vfs.provider.zip/close-zip-file.error :" + file, e);
      VfsLog.warn(getLogger(), log, "vfs.provider.zip/close-zip-file.error :" + file, e);
    }
  }

  /**
   * Returns the capabilities of this file system.
   */
  protected void addCapabilities(final Collection caps) {
    caps.addAll(FastJarFileProvider.capabilities);
  }
  
  private JarFile getJarFile() {
    return jarFile;
  }

  /**
   * Creates a file object.
   */
  protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
    if(!exists) {
      return new NonExistentJarFileObject(name, this);
    } else {
      String entryName = getRootName().getRelativeName(name);
      if(entryName.equals(".")) {
        return new FastJarRootFileObject(name, this);
      }

      JarEntry entry = getJarFile().getJarEntry(entryName + "/");
      if(entry != null) {
        return new FastJarFileObject(name, entry, this);
      }
      entry = getJarFile().getJarEntry(entryName);
      if(entry == null) {
        return new NonExistentJarFileObject(name, this);
      } else {
        return new FastJarFileObject(name, entry, this);
      }
    }
  }


  public String[] getRootItems() {
    throw new UnsupportedOperationException();
  }


  public InputStream getInputStream(JarEntry entry) throws FileSystemException {
    try {
      return getJarFile().getInputStream(entry);
    } catch (IOException e) {
      throw new FileSystemException(e);
    }
  }

  public String[] listChildren(String name) {
    JarFile jarFile = getJarFile();
    List<String> children = Lists.newArrayList();

    Enumeration<JarEntry> entries = jarFile.entries();
    while(entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if(!entry.getName().equals(name) && entry.getName().startsWith(name)) {
        String childName = entry.getName().substring(name.length());
        int slash = childName.indexOf('/');
        if(slash == -1) {
          children.add(childName);
        } else if(slash == (childName.length()-1)) {
          children.add(childName.substring(0, childName.length()-1));
        }
      }
    }
    return children.toArray(new String[children.size()]);
  }
}
