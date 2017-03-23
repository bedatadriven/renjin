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
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Unevaluated;
import org.renjin.sexp.Environment;
import org.renjin.sexp.ExternalPtr;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * Renjin-specific JVM interface primitives for interacting with 
 * Java/JVM objects
 * 
 */
public class Jvmi {

  private Jvmi() {
  }
  
  @Builtin("import")
  public static SEXP importClass(@Current Context context, @Current Environment rho, @Unevaluated Symbol className) {
        
    Class clazz;
    try {
      clazz = context.getSession().getClassLoader().loadClass(className.getPrintName());
    } catch (ClassNotFoundException e) {
      throw new EvalException("Cannot find class '%s'", className);
    }
    
    if(!context.getSession().getSecurityManager().allowNewInstance(clazz)) {
      throw new EvalException("Permission to create a new instance of class '%s' has been denied by the security manager",
          className);
    }

    ExternalPtr ptr = new ExternalPtr(clazz);
    rho.setVariable(context, Symbol.get(clazz.getSimpleName()), ptr);
    context.setInvisibleFlag();

    return ptr;
  }


  public static void addURL(URL u){
//    URLClassLoader sysloader = (URLClassLoader) ClassLoader
//        .getSystemClassLoader();
    URLClassLoader sysloader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
    try {
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] { u });
    } catch (Throwable t) {
      throw new EvalException("can not addURL %s into classLoader",u);
    }// end try catch
  }// end method
  
  /**
   * jload use to add classpath, fileName can be a folder or jar of class
   * @param context
   * @param rho
   * @param fileName
   * @return
   */
  @Builtin("jload")
  public static void jloadClass(@Current Context context, @Current Environment rho,
                                String fileName){
    try {
      File file = new File(fileName);
      if(!file.exists()){
        throw new EvalException("The file %s is not found %s",fileName,file.getAbsolutePath());
      } else {
        addURL(file.toURI().toURL());
      }
    } catch(MalformedURLException e) {
      throw new EvalException("No legal protocol could be found in %s ",fileName);
    }
  }
}
