package org.renjin.eval;

import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.renjin.util.FileSystemUtils;

public class SessionBuilder {

  private FileSystemManager fsm;
  private boolean loadBasePackage = true;
 
  public SessionBuilder withFileSystemManager(FileSystemManager fsm) {
    this.fsm = fsm;
    return this;
  }
  
  public SessionBuilder withoutBasePackage() {
    this.loadBasePackage = false;
    return this;
  }
  
  public Session build() {
    try {
      Session session = new Session(fsm == null ? FileSystemUtils.getMinimalFileSystemManager() : fsm);
      if(loadBasePackage) {
        session.getTopLevelContext().init();
      }
      return session;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public static Session buildDefault() {
    return new SessionBuilder().build();
  }
 
}
