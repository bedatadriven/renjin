package org.renjin.eval.vfs;


import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FastJarFileProviderTest {

  @Test
  public void test() throws FileSystemException, URISyntaxException {
    DefaultFileSystemManager fsm = new DefaultFileSystemManager();
    fsm.setDefaultProvider(new UrlFileProvider());
    fsm.addProvider("file", new DefaultLocalFileProvider());
    fsm.addProvider("jar", new FastJarFileProvider());
    fsm.init();

    String jarUri = getClass().getResource("/jarfiletest.jar").toURI().toString();
    FileObject object = fsm.resolveFile("jar:" + jarUri + "!/r/");

    assertThat(object.exists(), equalTo(true));
    assertThat(object.getType(), equalTo(FileType.FOLDER));

    FileObject[] children = object.getChildren();
    assertThat(children.length, equalTo(1));
    assertThat(children[0].getName().getBaseName(), equalTo("library"));
    assertThat(children[0].getType(), equalTo(FileType.FOLDER));

    object = fsm.resolveFile("jar:" + jarUri + "!/r/library/survey");

    assertThat(object.getType(), equalTo(FileType.FOLDER));
    assertThat(object.getChildren().length, equalTo(4));

  }

}
