package org.renjin.packaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

import r.lang.Context;
import r.lang.SEXP;
import r.parser.RParser;

public class BaseBuilder {

  public static void main(String[] args) throws IOException {
    
    File srcRoot = new File("src/library/base");
    File outDir = new File("target/classes/r/library/base");
    List<File> sources = PackagingUtils.findSourceFiles(srcRoot);
        
    Context context = Context.newTopLevelContext();
    context.getGlobals().setLibraryPaths("");
    
    for(File source : sources) {
      SEXP body = RParser.parseAllSource(new InputStreamReader(new FileInputStream(source)));
      context.evaluate(body, context.getGlobalEnvironment().getBaseEnvironment());
    }
    
    context.evaluate(RParser.parseAllSource(
        new InputStreamReader(new FileInputStream("src/library/base/makebasedb.R"))));
    
  }
  
}
