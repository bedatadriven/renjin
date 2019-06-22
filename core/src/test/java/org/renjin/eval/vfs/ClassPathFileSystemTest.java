package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.junit.Test;
import org.renjin.util.FileSystemUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ClassPathFileSystemTest {

  @Test
  public void testClasspathDir() throws FileSystemException {

    FileSystemManager fsm = FileSystemUtils.getMinimalFileSystemManager(getClass().getClassLoader());
    FileObject fileObject = fsm.resolveFile("classpath:///junit/runner");

    assertThat(fileObject.getType(), equalTo(FileType.FOLDER));
    assertTrue(fileObject.exists());

  }
}