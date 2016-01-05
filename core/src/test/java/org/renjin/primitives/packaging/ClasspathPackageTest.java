package org.renjin.primitives.packaging;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.junit.Test;
import org.renjin.util.FileSystemUtils;


public class ClasspathPackageTest {
  
  @Test
  public void resolvePackageRootTest() throws FileSystemException {

    FileSystemManager fileSystemManager = FileSystemUtils.getMinimalFileSystemManager();
    ClasspathPackage classpathPackage = new ClasspathPackage(new FqPackageName("org.renjin", "base"));

    FileObject fileObject = classpathPackage.resolvePackageRoot(fileSystemManager);
    for (FileObject object : fileObject.getChildren()) {
      System.out.println(object);
    }
  }
}