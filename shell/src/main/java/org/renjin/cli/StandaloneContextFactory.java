package org.renjin.cli;

import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.eval.Context;
import org.renjin.util.FileSystemUtils;

import java.io.File;

/**
 * Creates a Renjin context configured for "standalone mode" that
 * matches a traditional R context. The base package and installed
 * libraries are expected to be found in a filesystem directory rather than
 * strictly on the classpath.
 */
public class StandaloneContextFactory {

  private FileSystemManager manager;

  public StandaloneContextFactory() throws FileSystemException {
   
  }
  
  public Context create() throws FileSystemException {
    manager = FileSystemUtils.getMinimalFileSystemManager();
    String home = findRHome();

    Context topLevel = Context.newTopLevelContext(manager,
        home,
        FileSystemUtils.workingDirectory(manager));
    
    topLevel.getSession().setLibraryPaths(computeLibraryPaths(home));
    return topLevel;
  }
  
  private String computeLibraryPaths(String home) {
    StringBuilder libs = new StringBuilder();
    libs.append(computeUserHome());
    libs.append(";");
    libs.append(home).append("/library");
    return libs.toString();
  }

  private String computeUserHome() {
    File dir = new File( System.getProperty("user.home") + "/R/renjin/0.6.8" );
    dir.mkdirs();
    return dir.getAbsolutePath();
  }

  private String findRHome() throws FileSystemException {
    
    if(!Strings.isNullOrEmpty(System.getProperty("renjin.home"))) {
      return System.getProperty("renjin.home");
      
    } else {
      
      if(manager.resolveFile(FileSystemUtils.homeDirectoryInLocalFs()).exists()) {
        return FileSystemUtils.homeDirectoryInLocalFs();
      } else {
        return FileSystemUtils.homeDirectoryInCoreJar();
      }
    }
  }
}
