package org.renjin.eval;

import java.util.Map;

import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.util.FileSystemUtils;

import com.google.common.collect.Maps;

public class SessionBuilder {

  private boolean loadBasePackage = true;
  private Map<Class, Object> bindings = Maps.newHashMap();
 
  public SessionBuilder() {
    // set default bindings
    bind(PackageLoader.class, new ClasspathPackageLoader());
  }
  
  public SessionBuilder withFileSystemManager(FileSystemManager fsm) {
    bindings.put(FileSystemManager.class, fsm);
    return this;
  }
  
  public SessionBuilder withoutBasePackage() {
    this.loadBasePackage = false;
    return this;
  }
  
  /**
   * Binds a Renjin interface to its implementation
   * @param clazz
   * @param instance
   * @return
   */
  public <T> SessionBuilder bind(Class<T> clazz, T instance) {
    bindings.put(clazz, instance);
    return this;
  }
  
  public Session build() {
    try {
      if(!bindings.containsKey(FileSystemManager.class)) {
        bindings.put(FileSystemManager.class, FileSystemUtils.getMinimalFileSystemManager());
      }
         
      Session session = new Session(bindings);
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
