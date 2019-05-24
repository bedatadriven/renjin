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
package org.renjin.packaging;

import org.apache.commons.vfs2.FileSystemException;
import org.json.JSONObject;
import org.renjin.RenjinVersion;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.Deparse;
import org.renjin.primitives.io.serialization.HeadlessWriteContext;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.primitives.packaging.NamespaceFile;
import org.renjin.primitives.time.RDateTimeFormats;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.*;

import java.io.*;
import java.time.ZonedDateTime;

/**
 * Evaluates a package's sources
 */
public class NamespaceBuilder {

  private PackageSource source;
  private BuildContext buildContext;
  private final File environmentFile;

  public NamespaceBuilder(PackageSource source, BuildContext buildContext) {
    this.source = source;
    this.buildContext = buildContext;
    environmentFile = new File(buildContext.getPackageOutputDir(), "environment");
  }

  public void compile() throws IOException {
    
    Context context = initContext();

    Namespace namespace = context.getNamespaceRegistry().createNamespace(
        new InitializingPackage(
            source.getFqName(), 
            buildContext.getPackageOutputDir(),
            buildContext.getClassLoader()));

    loadDepends(context);
    importDependencies(context, namespace);
    loadPackageData(context, namespace);
    evaluateSources(context, namespace.getNamespaceEnvironment());
    invokeOnLoad(context, namespace.getNamespaceEnvironment());
    serializeEnvironment(context, namespace.getNamespaceEnvironment(), environmentFile);
    writeTransformedNamespace();
    writeRequires();
    writePackageRds();
    writeExecuteMetadata(namespace);
  }


  /**
   * Load packages in the Depends field onto the global search path
   */
  private void loadDepends(Context context) {
    
    if(source.getDescription() != null) {
      for (PackageDescription.PackageDependency dependency : source.getDescription().getDepends()) {
        if(!dependency.getName().equals("R")) {
          context.evaluate(FunctionCall.newCall(Symbol.get("library"), StringVector.valueOf(dependency.getName())));
        }
      }
    }
  }

  private void importDependencies(Context context, Namespace namespace) throws IOException {
    CharSource namespaceSource = Files.asCharSource(source.getNamespaceFile(), Charsets.UTF_8);
    ExpressionVector namespaceSexp = NamespaceQualifier.qualify(buildContext, NamespaceFile.parseSexp(namespaceSource));

    writeTransformedNamespace(context, namespaceSexp);

    NamespaceFile namespaceFile = NamespaceFile.parseFile(context, namespaceSexp);
    namespace.initImports(context, context.getNamespaceRegistry(), namespaceFile);
  }

  private void writeTransformedNamespace(Context context, ExpressionVector namespace) throws IOException {
    try(PrintWriter writer = new PrintWriter(new File(buildContext.getPackageOutputDir(), "NAMESPACE"))) {
      writer.println("# Transformed by Renjin " + RenjinVersion.getVersionName());
      for (SEXP statement : namespace) {
        writer.println(Deparse.deparseExp(context, statement));
      }
    }
  }

  private Context initContext()  {
    SessionBuilder builder = new SessionBuilder();
    builder.setPackageLoader(buildContext.getPackageLoader());
    builder.setClassLoader(buildContext.getClassLoader());

    Session session = builder.build();
    try {
      session.setWorkingDirectory(source.getPackageDir());
    } catch (FileSystemException e) {
      throw new BuildException("Could not set working directory to " + source.getPackageDir().getAbsolutePath(), e);
    }
    Context context = session.getTopLevelContext();
    for(String name : buildContext.getDefaultPackages()) {
      context.evaluate(FunctionCall.newCall(Symbol.get("library"), StringVector.valueOf(name)));
    }
    return context;
  }
  
  private void loadPackageData(Context context, Namespace namespace) throws IOException {
    File sysDataFile = new File(source.getSourceDir(), "sysdata.rda");
    if (sysDataFile.exists()) {
      buildContext.getLogger().info("Loading " + sysDataFile.getName());
      try {
        context.evaluate(FunctionCall.newCall(Symbol.get("load"), StringVector.valueOf(sysDataFile.getAbsolutePath())),
            namespace.getNamespaceEnvironment());
      } catch (EvalException e) {
        throw new IOException("Error evaluating sysdata.rda", e);
      }
    }
  }

  private void evaluateSources(Context context, Environment namespaceEnvironment)  {
    for(File sourceFile : source.getSourceFiles()) {
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

  private void invokeOnLoad(Context context, Environment namespaceEnvironment) {

    Symbol onLoad = Symbol.get(".onLoad");
    StringVector nameArgument = StringVector.valueOf(this.source.getPackageName());

    if(namespaceEnvironment.exists(onLoad)) {
      try {
        context.evaluate(FunctionCall.newCall(onLoad, nameArgument, nameArgument), namespaceEnvironment);
      } catch (Exception e) {
        if(e instanceof EvalException) {
          System.out.println("ERROR: " + e.getMessage());
          ((EvalException) e).printRStackTrace(System.out);
        }
        throw new RuntimeException("Exception evaluating .onLoad() method", e);
      }
    }
  }

  private void serializeEnvironment(Context context, Environment namespaceEnv, File environmentFile) {

    try {
      LazyLoadFrameBuilder builder = new LazyLoadFrameBuilder(context);
      builder.outputTo(environmentFile.getParentFile());
      builder.build(namespaceEnv);
    } catch(IOException e) {
      throw new RuntimeException("Exception encountered serializing namespace environment", e);
    }
  }


  private void writePackageRds() throws IOException {
    ListVector.NamedBuilder metadata = new ListVector.NamedBuilder();
    metadata.add("DESCRIPTION", descriptionVector());
    metadata.add("Built", new ListVector.NamedBuilder()
        .add("Platform", "")
        .add("Date", RDateTimeFormats.forPattern("yyyy-MM-dd HH:mm:ss ZZ").format(ZonedDateTime.now()))
        .add("OStype", "unix"));

    metadata.add("Rdepends", Null.INSTANCE);
    metadata.add("Rdepends2", Null.INSTANCE);

    metadata.add("Depends", packageVector("Depends"));
    metadata.add("Suggests", packageVector("Suggests"));
    metadata.add("Imports", packageVector("Imports"));
    metadata.add("LinkingTo", packageVector("LinkingTo"));


    File metaDir = new File(buildContext.getPackageOutputDir(), "Meta");
    if(!metaDir.exists()) {
      boolean created = metaDir.mkdirs();
      if(!created) {
        throw new IOException("Failed to create " + metaDir.getAbsolutePath());
      }
    }
    File packageRdsFile = new File(metaDir, "package.rds");
    try(RDataWriter writer = new RDataWriter(HeadlessWriteContext.INSTANCE, new FileOutputStream(packageRdsFile))) {
      writer.serialize(metadata.build());
    }
  }

  private StringVector descriptionVector() {
    StringVector.Builder vector = new StringVector.Builder();
    StringVector.Builder names = new StringVector.Builder();

    PackageDescription description = source.getDescription();
    if(description != null) {
      for (String property : description.getProperties()) {
        names.add(property);
        vector.add(description.getFirstProperty(property));
      }
    }

    vector.setAttribute(Symbols.NAMES, names.build());
    return vector.build();
  }

  private ListVector packageVector(String type) {
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    if(source.getDescription() != null) {
      for (PackageDescription.PackageDependency dependency : source.getDescription().getPackageDependencyList(type)) {
        ListVector.NamedBuilder depSexp = new ListVector.NamedBuilder();
        depSexp.add("name", dependency.getName());
        list.add(dependency.getName(), depSexp.build());
      }
    }
    return list.build();
  }


  private void writeTransformedNamespace() {

  }

  private void writeExecuteMetadata(Namespace namespace) throws IOException {
    SEXP execute = namespace.getNamespaceEnvironment().getVariableUnsafe(Symbol.get("execute"));
    if(execute instanceof Closure) {

      // Write a JSON metadata file that can be used by consumers to get information
      // on this job's parameters

      JSONObject metadata = ExecuteMetadataBuilder.composeMetadata(source, (Closure) execute);
      File metadataFile = buildContext.getExecuteMetadataFile();
      Files.write(metadata.toString(2), metadataFile, Charsets.UTF_8);

      // Write a simple text file with the name of the namespace that contains the execute function
      Files.write(source.getFqName().toString(), buildContext.getExecuteNamespaceFile(), Charsets.UTF_8);
    }
  }


  private void writeRequires() {
    // save a list of packages that are to be loaded onto the
    // global search path when this package is loaded

    Iterable<PackageDescription.PackageDependency> depends = source.getDescription().getDepends();

    if(!Iterables.isEmpty(depends)) {
      try {
        PrintWriter requireWriter = new PrintWriter(new File(buildContext.getPackageOutputDir(), "requires"));
        for(PackageDescription.PackageDependency dep : depends) {
          if(!dep.getName().equals("R") && !Strings.isNullOrEmpty(dep.getName())) {
            requireWriter.println(dep.getName());
          }
        }
        requireWriter.close();
      } catch (IOException e) {
        throw new RuntimeException("Exception writing requires file", e);
      }
    }
  }
}

