/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.compiler;

import com.google.common.base.Preconditions;
import org.apache.commons.cli.*;
import r.lang.EnvExp;
import r.lang.ExpExp;
import r.lang.GlobalContext;
import r.lang.SymbolTable;
import r.lang.exception.EvalException;
import r.parser.RParser;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

/**
 * The R Compiler works by generating Java source code that 
 * constructs the R expression tree.
 *
 * A this point, the resulting JVM classes only replace the
 * parsing stage, but ultimately the goal is optimize the expression tree
 * and provide subclasses of {@code ClosureExp} that evaluate more efficiently.
 */
public class Compiler {

  private final GlobalContext globalContext;
  private final EnvExp targetEnvironment;
  private final PackageSource source;

  private String packageName;
  private String classOutputDir;
  private String sourceOutputDir;
  private int sourceFileCount;


  public Compiler() {
    globalContext = new GlobalContext();
    targetEnvironment = new EnvExp(globalContext.getBaseEnvironment());
    source = new PackageSource();
  }

  /**
   * Adds a list of R source files to the class to be compiled
   *
   * @param sourcePaths
   * @throws IOException
   */
  public void addSources(Iterable<String> sourcePaths) throws IOException {
    for(String path : sourcePaths) {
      addSource(path);
    }
  }

  /**
   * Adds an individual source file to the output class
   *
   * @param path
   * @throws IOException
   */
  public void addSource(String path) throws IOException {
    addSource(new File(path));
  }

  public void addSource(File sourceFile) throws IOException {
    try {
      FileInputStream stream = new FileInputStream(sourceFile);
      Reader reader = new InputStreamReader(stream);

      addSource(reader);
    } catch (EvalException e) {
      System.err.println(String.format("Evaluation error in %s:\n\t%s", sourceFile.getName(), 
          e.getMessage()));
      throw e;
    } catch (RuntimeException e) {
      System.err.println(String.format("RuntimeException in %s", sourceFile.getName()));
      e.printStackTrace(System.err);
      throw e;
    }
  }

  /**
   * Adds an individual source file to the output class
   * @param reader a {@code Reader} for the source file
   * @throws IOException
   */
  public void addSource(Reader reader) throws IOException {
    sourceFileCount++;
    ExpExp expList = RParser.parseSource(globalContext, reader);
    expList.evaluate(targetEnvironment);
  }

  public int getSourceFileCount() {
    return sourceFileCount;
  }

  public EnvExp getTargetEnvironment() {
    return targetEnvironment;
  }

  /**
   * Adds all *.R source files in the given directory to the
   * output class
   *
   * @param dir a directory containing *.R source files
   */
  private void addSourceDirectory(File dir) throws IOException {
    Preconditions.checkArgument(dir.exists(),
        String.format("Source directory %s does not exist", dir.getAbsolutePath()));
    Preconditions.checkArgument(dir.isDirectory(),
        String.format("Source directory %s is not a directory", dir.getAbsolutePath()));

    for(File source : dir.listFiles()) {
      if(source.getName().endsWith(".R")) {
        addSource(source);
      }
    }
  }


  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String className() {
    Preconditions.checkNotNull(packageName, "packageName must be set");

    int dot = packageName.lastIndexOf('.');
    return packageName.substring(dot+1,dot+2).toUpperCase() +
        packageName.substring(dot+2);
  }

  public File packageDir(File relativeTo) {
    return new File(relativeTo, packageName.replace('.', File.separatorChar));
  }

  public String getClassOutputDir() {
    return classOutputDir;
  }

  public void setClassOutputDir(String classOutputDir) {
    this.classOutputDir = classOutputDir;
  }

  public String getSourceOutputDir() {
    return sourceOutputDir;
  }

  public void setSourceOutputDir(String sourceOutputDir) {
    this.sourceOutputDir = sourceOutputDir;
  }

  public void compile() throws IOException {
    Preconditions.checkArgument(classOutputDir!=null, "classOutputDir must be set");

    File sourceFile = writeSource();
    compileSource(sourceFile);
  }

  public EnvExp load(EnvExp rho) {
    URLClassLoader loader = null;
    try {
      loader = new URLClassLoader(new URL[] { new File(classOutputDir).toURI().toURL() } );

      Class<EnvExp> context = (Class<EnvExp>) loader.loadClass(packageName + "." + className());
      return context.getConstructor(EnvExp.class).newInstance(rho);

    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }


  public File writeSource() throws FileNotFoundException {
    Preconditions.checkNotNull(packageName, "packageName must be set");

    File sourceDir = sourceOutputDir == null ? new File(".") : new File(sourceOutputDir);
    sourceDir = packageDir(sourceDir);
    sourceDir.mkdirs();

    SymbolTable symbolTable = globalContext.getSymbolTable();
    for(String symbolName : targetEnvironment.getSymbolNames()) {
      source.addSymbol(symbolName, targetEnvironment.findVariable(symbolTable.install(symbolName)));
    }

    File sourceFile = new File(sourceDir, className() + ".java");
    source.writeTo(packageName, className(), new PrintStream(sourceFile));

    return sourceFile;
  }


  private void compileSource(File sourceFile) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    StandardJavaFileManager jfm = compiler.getStandardFileManager(diagnostics, null, null);
    File outputDir = new File(classOutputDir);
    outputDir.mkdirs();

    jfm.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(outputDir));

    JavaCompiler.CompilationTask task = compiler.getTask(null, jfm, diagnostics, null, null,
        Collections.singleton(new TranslatedSourceObject(sourceFile)));

    boolean success = task.call();

    jfm.close();

    if(!success) {
      StringBuilder message = new StringBuilder();
      message.append("Compilation of the translated R sources failed. This is probably\n")
          .append("an issue with the compiler, not your R sources.\n")
          .append("Please consider filing an issue at: \n\n")
          .append("http://code.google.com/p/renjin/issues/entry?template=CompilerError")
          .append("\n\n")
          .append("Here are the errors from javac:\n");

      for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
        message.append("[").append(diagnostic.getKind()).append("] ")
            .append(cleanupMessage(diagnostic.getMessage(null)))
            .append("\n");
    
      }

      throw new RuntimeException(message.toString());
    }
  }


  private class TranslatedSourceObject extends SimpleJavaFileObject {
    private final File source;

    private TranslatedSourceObject(File source) {
      super(source.toURI(), Kind.SOURCE);
      this.source = source;
    }
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(source));
        StringBuffer code = new StringBuffer();
        while (reader.ready()) {
          code.append(reader.readLine()).append("\n");
        }
        return code;
      } catch (Exception e) {
        throw new AssertionError(e);
      }
    }
  }

  public static void main(String[] arguments) {
    Options options = new Options();
    options.addOption( OptionBuilder.withArgName("package")
        .hasArg()
        .withDescription("Name of the (JVM) output package (e.g. r.packages.base")
        .create("package"));

    options.addOption( OptionBuilder.withArgName("path")
        .hasArg()
        .withDescription("Specify where to find input source files")
        .create("sourcepath"));

    options.addOption( OptionBuilder.withArgName("directory")
        .hasArg()
        .withDescription("Specify where to place generated source files")
        .create("s"));
    options.addOption( OptionBuilder.withArgName("directory")
        .hasArg()
        .withDescription("Specify where to place generated class files")
        .create("d"));



    CommandLineParser parser = new PosixParser();
    try {
      System.out.println("r.compiler.Compiler starting...");
      CommandLine commandLine = parser.parse(options, arguments);

      if( commandLine.getArgList().size() == 0 && ! commandLine.hasOption("sourcepath")) {
        throw new ParseException("Either a -sourcepath or a list of input files must be provided");
      }
      if( ! commandLine.hasOption("package") ) {
        throw new ParseException("You must specify the package");
      }

      Compiler compiler = new Compiler();
      compiler.setClassOutputDir( commandLine.getOptionValue("d") );
      compiler.setPackageName( commandLine.getOptionValue("package") );
      compiler.setSourceOutputDir( commandLine.getOptionValue("s") );
      if( commandLine.getArgList().isEmpty() ) {
        compiler.addSourceDirectory( new File(commandLine.getOptionValue("sourcepath")) );
      }

      compiler.addSources( commandLine.getArgList() );
      compiler.compile();
      System.out.println("Compiled " + compiler.getSourceFileCount() + " R sources.");


    } catch (ParseException e) {
      System.out.println( e.getMessage() );
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Compiler", options );

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String cleanupMessage(String message) {
    int colon = message.indexOf(':', 5);
    if (colon == -1)
      return message;
    int pathStart = message.substring(0, colon).lastIndexOf('/');
    return message.substring(pathStart + 1);

  }
}
