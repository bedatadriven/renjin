package org.renjin.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.primitives.packaging.NamespaceFile;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Evaluates a package's sources
 */
public class NamespaceBuilder2 {

  private PackageSource source;
  private BuildContext buildContext;
  private final File environmentFile;

  public NamespaceBuilder2(PackageSource source, BuildContext buildContext) {
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
    
    importDependencies(context, namespace);
    evaluateSources(context, namespace.getNamespaceEnvironment());
    serializeEnvironment(context, namespace.getNamespaceEnvironment(), environmentFile);
    writeRequires();
  }

  private void importDependencies(Context context, Namespace namespace) throws IOException {

    CharSource namespaceSource = Files.asCharSource(source.getNamespaceFile(), Charsets.UTF_8);
    NamespaceFile namespaceFile = NamespaceFile.parse(context, namespaceSource);

    namespace.initImports(context, context.getNamespaceRegistry(), namespaceFile);
  }

  private Context initContext()  {
    SessionBuilder builder = new SessionBuilder();
    builder.bind(PackageLoader.class, buildContext.getPackageLoader());
    
    Context context = builder.build().getTopLevelContext();
    for(String name : buildContext.getDefaultPackages()) {
      context.evaluate(FunctionCall.newCall(Symbol.get("library"), StringVector.valueOf(name)));
    }
    return context;
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



  private void writeRequires() {
    // save a list of packages that are to be loaded onto the
    // global search path when this package is loaded

    if(source.getDescriptionFile().exists()) {
      PackageDescription description;
      try {
        description = PackageDescription.fromFile(source.getDescriptionFile());
      } catch(IOException e) {
        throw new RuntimeException("Exception reading DESCRIPTION file");
      }
      try {
        PrintWriter requireWriter = new PrintWriter(new File(buildContext.getPackageOutputDir(), "requires"));
        for(PackageDescription.PackageDependency dep : description.getDepends()) {
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

