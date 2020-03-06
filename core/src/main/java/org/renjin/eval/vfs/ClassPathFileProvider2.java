package org.renjin.eval.vfs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Provides a virtual filesystem over the classpath.
 *
 */
public class ClassPathFileProvider2 extends AbstractOriginatingFileProvider {

  final static Collection<Capability> CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(
      Capability.GET_LAST_MODIFIED,
      Capability.GET_TYPE,
      Capability.READ_CONTENT,
      Capability.LIST_CHILDREN,
      Capability.URI,
      Capability.VIRTUAL));

  private final ClassLoader classLoader;

  public ClassPathFileProvider2(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  protected FileSystem doCreateFileSystem(FileName rootName, FileSystemOptions fileSystemOptions) {
    return new ClassPathFileSystem(classLoader, rootName, fileSystemOptions);
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return CAPABILITIES;
  }

}
