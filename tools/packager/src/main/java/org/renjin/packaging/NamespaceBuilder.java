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
package org.renjin.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.primitives.packaging.NamespaceFile;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Evaluates a package's sources
 */
public class NamespaceBuilder {

  private FqPackageName name;
  private File namespaceFile;
  private List<File> sources;
  private File environmentFile;
  private List<String> defaultPackages;

  public void build(String groupId, String namespaceName, 
                    File namespaceFile, 
                    List<File> sourceFiles,
                    File environmentFile, List<String> defaultPackages) throws IOException {

    this.name = new FqPackageName(groupId, namespaceName);
    this.namespaceFile = namespaceFile;
    this.sources = sourceFiles;
    this.environmentFile = environmentFile;
    this.defaultPackages = defaultPackages;

    compileNamespaceEnvironment();
  }

  private void compileNamespaceEnvironment() throws IOException {
    if(isUpToDate(sources)) {
      return;
    }

    Context context = initContext();

    Namespace namespace = context.getNamespaceRegistry().createNamespace(
        new InitializingPackage(name, environmentFile.getParentFile(), getClass().getClassLoader()));
    importDependencies(context, namespace);
    evaluateSources(context, namespace.getNamespaceEnvironment());
    serializeEnvironment(context, namespace.getNamespaceEnvironment(), environmentFile);
  }

  private void importDependencies(Context context, Namespace namespace) throws IOException {

    CharSource namespaceSource = Files.asCharSource(namespaceFile, Charsets.UTF_8);
    NamespaceFile namespaceFile = NamespaceFile.parse(context, namespaceSource);

    namespace.initImports(context, context.getNamespaceRegistry(), namespaceFile);
  }

  private boolean isUpToDate(List<File> sources) {
    long lastModified = 0;
    for(File source : sources) {
      if(source.lastModified() > lastModified) {
        lastModified = source.lastModified();
      }
    }

    if(lastModified < environmentFile.lastModified()) {
      System.out.println("namespaceEnvironment is up to date, skipping compilation");
      return true;
    }

    return false;
  }

  private Context initContext()  {
    SessionBuilder builder = new SessionBuilder();
    Context context = builder.build().getTopLevelContext();
    if(defaultPackages != null) {
      for(String name : defaultPackages) {
        context.evaluate(PairList.Node.newCall(Symbol.get("library"), StringVector.valueOf(name)));
      }
    }
    return context;
  }


  private void evaluateSources(Context context, Environment namespaceEnvironment)  {
    for(File sourceFile : sources) {
      System.err.println("Evaluating '" + sourceFile + "'");
      try {
        FileReader reader = new FileReader(sourceFile);
        SEXP expr = RParser.parseAllSource(reader);
        reader.close();

        context.evaluate(expr, namespaceEnvironment);

      } catch (EvalException e) {
        System.out.println("ERROR: " + e.getMessage());
        e.printRStackTrace(System.out);
        throw new RuntimeException("Error evaluating package source: " + sourceFile.getName(), e);
      } catch (Exception e) {
        throw new RuntimeException("Exception evaluating " + sourceFile.getName(), e);
      }
    }
  }

  private void serializeEnvironment(Context context, Environment namespaceEnv, File environmentFile) {

    System.out.println("Writing namespace environment to " + environmentFile);
    try {
      LazyLoadFrameBuilder builder = new LazyLoadFrameBuilder(context);
      builder.outputTo(environmentFile.getParentFile());
      builder.build(namespaceEnv);
    } catch(IOException e) {
      throw new RuntimeException("Exception encountered serializing namespace environment", e);
    }
  }

}

