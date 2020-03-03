package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a logical classpath that includes multiple physical directories, either
 * directories on the classpath, or potentially multiple Jar files.
 */
public class CompositeClassDirectoryObject extends AbstractFileObject {

  private final List<File> localDirs;
  private final Set<String> children;

  public CompositeClassDirectoryObject(AbstractFileName name, AbstractFileSystem fileSystem, List<File> localDirs, Set<String> children) {
    super(name, fileSystem);
    this.localDirs = localDirs;
    this.children = children;
  }

  @Override
  protected FileType doGetType() {
    return FileType.FOLDER;
  }

  @Override
  protected String[] doListChildren() {
    Set<String> names = new HashSet<>(children);

    for (File localDir : localDirs) {
      File[] files = localDir.listFiles();
      if(files != null) {
        for (File localFile : files) {
          names.add(localFile.getName());
        }
      }
    }
    return names.toArray(new String[0]);
  }

  @Override
  protected long doGetContentSize() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  protected InputStream doGetInputStream() {
    throw new UnsupportedOperationException("TODO");
  }
}
