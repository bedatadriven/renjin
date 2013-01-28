package org.renjin.primitives;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.jvminterop.ClassBinding;
import org.renjin.jvminterop.ClassFrame;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Evaluate;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;


/**
 * Renjin-specific JVM interface primitives for interacting with 
 * Java/JVM objects
 * 
 */
public class Jvmi {

  private Jvmi() {
    
  }
  
  @Primitive("import")
  public static SEXP importClass(@Current Context context, @Current Environment rho, 
        @Evaluate(false) Symbol className) {
        
    //TODO to suport import(org.apache.hadoop.io.*)
    Class clazz;
    try {
      clazz = Class.forName(className.getPrintName());
    } catch (ClassNotFoundException e) {
      throw new EvalException("Cannot find class '%s'", className);
    }
    
    if(!context.getSession().getSecurityManager().allowNewInstance(clazz)) {
      throw new EvalException("Permission to create a new instance of class '%s' has been denied by the security manager",
          className);
    }

    Environment env = Environment.createChildEnvironment(Environment.EMPTY, 
            new ClassFrame(ClassBinding.get(clazz)));
    
    rho.setVariable(Symbol.get(clazz.getSimpleName()), env);
    
    context.setInvisibleFlag();
    
    return env;
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
  @Primitive("jload")
  public static void jloadClass(@Current Context context, @Current Environment rho, 
        String fileName){
    try{
        File file = new File(fileName);
        if(!file.exists()){
          throw new EvalException("The file %s is not found %s",fileName,file.getAbsolutePath());
        }else{
          addURL(file.toURI().toURL());
        }
    }
    catch(MalformedURLException e){
      throw new EvalException("No legal protocol could be found in %s ",fileName);
    }
  }
}
