package org.renjin.base;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.packaging.LazyLoadFrameBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

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
      SEXP expr = RParser.parseAllSource(reader);
      reader.close();
      evalContext.evaluate(expr);
    }
  }  
  
}
