/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.eval;

import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;
import org.renjin.util.FileSystemUtils;

import java.util.List;
import java.util.Map;

public class SessionBuilder {

  private boolean loadBasePackage = true;
  private Map<Class, Object> bindings = Maps.newHashMap();
  private List<String> packagesToLoad = Lists.newArrayList();
 
  public SessionBuilder() {
    // set default bindings
    bind(PackageLoader.class, new ClasspathPackageLoader());
  }
  
  public SessionBuilder withFileSystemManager(FileSystemManager fsm) {
    bindings.put(FileSystemManager.class, fsm);
    return this;
  }
  
  /**
   * Disables loading of the R-Language portions of the base package:
   * primitives will still be available but none of the functions in the base 
   * package will be loaded. 
   */
  public SessionBuilder withoutBasePackage() {
    this.loadBasePackage = false;
    return this;
  }
  
  /**
   * Loads the default packages for R 2.14.2 (stats, utils, graphics, grDevices, datasets, methods)
   */
  public SessionBuilder withDefaultPackages() {
    packagesToLoad = Session.DEFAULT_PACKAGES;
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
      for(String packageToLoad : packagesToLoad) {
        session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("library"),
            Symbol.get(packageToLoad)));
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
