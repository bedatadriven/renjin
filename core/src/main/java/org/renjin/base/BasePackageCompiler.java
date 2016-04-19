package org.renjin.base;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.packaging.LazyLoadFrameBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Bootstraps the packaging of the base R package
 *
 */
public class BasePackageCompiler {

  public static void main(String[] args) throws IOException {
 
    // Evaluate the base sources into the base namespace environment

    Session session = new SessionBuilder()
        .withoutBasePackage()
        .build();
    
    Context context = session.getTopLevelContext();
    Environment baseNamespaceEnv = context.getNamespaceRegistry().getBase().getNamespaceEnvironment();
    Context evalContext = context.beginEvalContext(baseNamespaceEnv);
    
    File baseSourceRoot = new File("src/main/R/base");
    evalSources(evalContext, baseSourceRoot);
    
    evalContext.evaluate(FunctionCall.newCall(Symbol.get(".onLoad")));  
    
    // now serialize them to a lazy-loadable frame
    
    final List<String> omit = Lists.newArrayList(
        ".Last.value", ".AutoloadEnv", ".BaseNamespaceEnv", 
        ".Device", ".Devices", ".Machine", ".Options", ".Platform");

    new LazyLoadFrameBuilder(context)
        .outputTo(new File("target/classes/org/renjin/base"))
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

  private static void evalSources(Context evalContext, File dir) throws IOException {
    List<File> sources = Lists.newArrayList();
    for(File sourceFile : dir.listFiles()) {
      if(sourceFile.getName().endsWith(".R")) {
        sources.add(sourceFile);
      }
    }
    Collections.sort(sources);
    
    for(File source : sources) {
      FileReader reader = new FileReader(source);
      try {
        SEXP expr = RParser.parseAllSource(reader);
        evalContext.evaluate(expr);
      } catch(Exception e) {
        throw new RuntimeException("Error evaluating " + source.getName() + ": " + e.getMessage(), e);
      } finally {
        reader.close();        
      }
    }
  }  
  
}
