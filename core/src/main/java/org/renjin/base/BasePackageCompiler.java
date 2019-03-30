/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.base;

import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.packaging.LazyLoadFrameBuilder;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Bootstraps the packaging of the base R package
 *
 */
public class BasePackageCompiler {

  public static void main(String[] args) throws IOException {

    String outputDir;
    if(args.length == 1) {
      outputDir = args[0] + "/org/renjin/base";
    } else {
      outputDir = "target/classes/org/renjin/base";
    }

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
    
    final Set<String> omit = Sets.newHashSet(
        ".Last.value", ".AutoloadEnv", ".BaseNamespaceEnv", 
        ".Device", ".Devices", ".Machine", ".Options", ".Platform");

    new LazyLoadFrameBuilder(context)
        .outputTo(new File(outputDir))
        .excludeSymbols(omit)
        .filter(x -> !(x instanceof PrimitiveFunction))
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
      try {
        SEXP expr = RParser.parseSource(Files.asCharSource(source, Charsets.UTF_8), source.getName());
        evalContext.evaluate(expr);
      } catch(Exception e) {
        throw new RuntimeException("Error evaluating " + source.getName() + ": " + e.getMessage(), e);
      }
    }
  }  
  
}
