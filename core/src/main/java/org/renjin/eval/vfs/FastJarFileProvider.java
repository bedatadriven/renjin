package org.renjin.eval.vfs;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractLayeredFileProvider;
import org.apache.commons.vfs2.provider.LayeredFileName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Fast(er) Replacement for the CommonsVFS Jar File provider.
 *
 * <p>The Commons VFS implementation starts by building a list of FileObjects
 * for all jar entries in the jar, even if they're never accessed. This seems
 * to be contributing to delays in start up time.</p>
 *
 * <p>This implementation creates </p>
 *
 */
public class FastJarFileProvider extends AbstractLayeredFileProvider {


  final static Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays.asList(
      Capability.GET_LAST_MODIFIED,
      Capability.GET_TYPE,
      Capability.LIST_CHILDREN,
      Capability.READ_CONTENT,
      Capability.URI,
      Capability.COMPRESS,
      Capability.VIRTUAL));


  /**
   * Creates a layered file system.  This method is called if the file system
   * is not cached.
   *
   * @param scheme The URI scheme.
   * @param file   The file to create the file system on top of.
   * @return The file system.
   */
  protected FileSystem doCreateFileSystem(final String scheme,
                                          final FileObject file,
                                          final FileSystemOptions fileSystemOptions)
      throws FileSystemException
  {
    final FileName rootName =
        new LayeredFileName(scheme, file.getName(), FileName.ROOT_PATH, FileType.FOLDER);
    return new FastJarFileSystem(rootName, file, fileSystemOptions);
  }

  public Collection<Capability> getCapabilities() {
    return capabilities;
  }
}
