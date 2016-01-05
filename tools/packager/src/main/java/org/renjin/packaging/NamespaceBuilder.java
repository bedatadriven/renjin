package org.renjin.packaging;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.primitives.packaging.NamespaceFile;
import org.renjin.sexp.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NamespaceBuilder {

  private FqPackageName name;
  private File namespaceFile;
  private File sourceDirectory;
  private List<String> sourceFiles;
  private File environmentFile;
  private List<String> defaultPackages;

  public void build(String groupId, String namespaceName, File namespaceFile,
                    File sourceDirectory, List<String> sourceFiles,
                    File environmentFile, List<String> defaultPackages) throws IOException {

    this.name = new FqPackageName(groupId, namespaceName);
    this.namespaceFile = namespaceFile;
    this.sourceDirectory = sourceDirectory;
    this.sourceFiles = sourceFiles;
    this.environmentFile = environmentFile;
    this.defaultPackages = defaultPackages;

    compileNamespaceEnvironment();
  }


  private void compileNamespaceEnvironment() throws IOException {
    List<File> sources = getRSources();
    if(isUpToDate(sources)) {
      return;
    }

    Context context = initContext();

    Namespace namespace = context.getNamespaceRegistry().createNamespace(
        new InitializingPackage(name, environmentFile.getParentFile()));
    importDependencies(context, namespace);
    evaluateSources(context, getRSources(), namespace.getNamespaceEnvironment());
    serializeEnvironment(context, namespace.getNamespaceEnvironment(), environmentFile);
  }

  private void importDependencies(Context context, Namespace namespace) throws IOException {

    CharSource namespaceSource = Files.asCharSource(namespaceFile, Charsets.UTF_8);
    NamespaceFile namespaceFile = NamespaceFile.parse(context, namespaceSource);

    namespace.initImports(context.getNamespaceRegistry(), namespaceFile);

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
        context.evaluate(FunctionCall.newCall(Symbol.get("library"), StringVector.valueOf(name)));
      }
    }
    return context;
  }

  private List<File> getRSources() {

    if(sourceFiles != null) {
      // explicitly specified in POM file
      return sourceListFromPom();

    } else {
      return findSources();
    }
  }


  private List<File> sourceListFromPom() {
    List<File> list = Lists.newArrayList();
    for (String sourceFilename : sourceFiles) {
      File sourceFile = new File(sourceDirectory, sourceFilename);
      if(!sourceFile.exists()) {
        throw new RuntimeException("Source file '" + sourceFile.getAbsolutePath() + "' does not exist.");
      }
      list.add(sourceFile);
    }
    return list;
  }

  private List<File> findSources() {
    List<File> list = Lists.newArrayList();

    // all .R/.S files in the R sourceDirectory
    File[] files = sourceDirectory.listFiles();
    if (files != null) {
      for (File file : files) {

        String nameUpper = file.getName().toUpperCase();

        if (nameUpper.endsWith(".R") ||
            nameUpper.endsWith(".S") ||
            nameUpper.endsWith(".Q")) {

          list.add(file);

        }
      }
    }
    
    // Sort by filename, IGNORING extension
    Collections.sort(list, new Comparator<File>() {
      @Override
      public int compare(File file1, File file2) {
        String name1 = Files.getNameWithoutExtension(file1.getName()).toLowerCase();
        String name2 = Files.getNameWithoutExtension(file2.getName()).toLowerCase();
        return name1.compareTo(name2);
      }
    });
    
    return list;
  }

  private void evaluateSources(Context context, List<File> sources, Environment namespaceEnvironment)  {
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

