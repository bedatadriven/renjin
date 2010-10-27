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
import r.compiler.runtime.Program;
import r.lang.ExpExp;
import r.lang.GlobalContext;
import r.parser.RParser;

import javax.tools.*;
import java.io.*;
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
  private final JavaSourceWritingVisitor sourceWritingVisitor;

  private String packageName = "r.packages";
  private String className;
  private String classOutputDir;
  private String sourceOutputDir;

  public Compiler() {
    globalContext = new GlobalContext();
    sourceWritingVisitor = new JavaSourceWritingVisitor();
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


  public void addSource(String path) throws IOException {
    FileInputStream stream = new FileInputStream(path);
    Reader reader = new InputStreamReader(stream);

    addSource(reader);
  }

  public void addSource(Reader reader) throws IOException {
    ExpExp expList = RParser.parseSource(globalContext, reader);
    expList.accept(sourceWritingVisitor);
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
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

  public Program load() {
    URLClassLoader loader = null;
    try {
      loader = new URLClassLoader(new URL[] { new File(classOutputDir).toURI().toURL() } );

      Class<Program> context = (Class<Program>) loader.loadClass(packageName + "." + className);
      return context.newInstance();

    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }


  public File writeSource() throws FileNotFoundException {
    File sourceDir = sourceOutputDir == null ? new File(".") : new File(sourceOutputDir);
    if(sourceDir.exists()) {
      sourceDir.mkdirs();
    }
    File sourceFile = new File(sourceDir, className + ".java");
    sourceWritingVisitor.writeTo(packageName, className, new PrintStream(sourceFile));

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

    for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
      System.out.println("[" + diagnostic.getKind() + "] " +
          cleanupMessage(diagnostic.getMessage(null)));
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
    options.addOption( OptionBuilder.withArgName("className")
        .hasArg()
        .withDescription("Specify the name of the generated class")
        .create());
    options.addOption( OptionBuilder.withArgName("s")
        .hasArg()
        .withDescription("Specify where to place generated source files")
        .create());
    options.addOption( OptionBuilder.withArgName("d")
        .hasArg()
        .withDescription("Specify where to place generated class files")
        .create());

    CommandLineParser parser = new PosixParser();
    try {
      CommandLine commandLine = parser.parse(options, arguments);
      Compiler compiler = new Compiler();
      compiler.setClassOutputDir( commandLine.getOptionValue("d") );
      compiler.setClassName( commandLine.getOptionValue("className"));
      compiler.addSources( commandLine.getArgList() );

    } catch (ParseException e) {
      System.out.println( "Unexpected exception: " + e.getMessage() );
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
