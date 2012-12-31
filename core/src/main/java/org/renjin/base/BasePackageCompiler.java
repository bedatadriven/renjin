package org.renjin.base;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.renjin.eval.Context;
import org.renjin.packaging.LazyLoadFrameBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * Bootstraps the packaging of the base R package
 *
 */
public class BasePackageCompiler {

  public static void main(String[] args) throws IOException {
 
    // Evaluate the base sources into the base namespace environment
  
    Context context = Context.newTopLevelContext();
    Environment baseNamespaceEnv = context.getNamespaceRegistry().getBase().getNamespaceEnvironment();
    Context evalContext = context.beginEvalContext(baseNamespaceEnv);
    
    File baseSourceRoot = new File("src/main/R/base");
    List<File> baseSources = Lists.newArrayList();
    for(File sourceFile : baseSourceRoot.listFiles()) {
      if(sourceFile.getName().endsWith(".R")) {
        baseSources.add(sourceFile);
      }
    }
    Collections.sort(baseSources);
    
    for(File baseSource : baseSources) {
      FileReader reader = new FileReader(baseSource);
      SEXP expr = RParser.parseAllSource(reader);
      reader.close();
      evalContext.evaluate(expr);
    }
    
    // now serialize them to a lazy-loadable frame
    
    final List<String> omit = Lists.newArrayList(
        ".Last.value", ".AutoloadEnv", ".BaseNamespaceEnv", 
        ".Device", ".Devices", ".Machine", ".Options", ".Platform");
    
    new LazyLoadFrameBuilder(context)
    .outputTo(new File("target/classes/org/renjin/baseNamespace"))
    .filter(new Predicate<NamedValue>() {
      public boolean apply(NamedValue namedValue) {
        if(omit.contains(namedValue.getName())) {
          return false;
        }
        if(namedValue.getValue() instanceof PrimitiveFunction) {
          return false;
        }
        return true;
      }
    })
    .build(baseNamespaceEnv);
  }  
}
