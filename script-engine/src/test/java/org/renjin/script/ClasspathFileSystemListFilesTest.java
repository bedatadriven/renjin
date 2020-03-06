package org.renjin.script;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.junit.Test;
import org.renjin.util.FileSystemUtils;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ClasspathFileSystemListFilesTest {

  @Test
  public void test() throws FileSystemException {
    FileSystemManager fsm = FileSystemUtils.getMinimalFileSystemManager(getClass().getClassLoader());
    FileObject fileObject = fsm.resolveFile("classpath:///org/renjin");

    assertThat(fileObject.getType(), equalTo(FileType.FOLDER));
    assertTrue(fileObject.exists());

    Set<String> childNames = new HashSet<>();
    for (FileObject child : fileObject.getChildren()) {
      System.out.println(child.getName().toString());
      childNames.add(child.getName().getBaseName());
    }

    assertTrue(childNames.contains("stats"));
    assertTrue(childNames.contains("repackaged"));
    assertTrue(childNames.contains("nmath"));
    assertTrue(childNames.contains("methods"));

  }
}
