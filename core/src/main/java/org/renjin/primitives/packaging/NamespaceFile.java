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
package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Parses directives in the NAMESPACE file.
 *
 * <p>The NAMESPACE file uses R syntax but is not necessarily an R script and so is parsed
 * and handled specially.</p>
 */
public class NamespaceFile {



  public static class DynLibEntry {
    private String libraryName;
    private String prefix = "";
    private boolean registration;

    private List<DynLibSymbol> symbols = new ArrayList<DynLibSymbol>();

    public String getLibraryName() {
      return libraryName;
    }

    public String getPrefix() {
      return prefix;
    }

    public boolean isRegistration() {
      return registration;
    }

    public List<DynLibSymbol> getSymbols() {
      return symbols;
    }
  }

  public static class DynLibSymbol {
    private String alias;
    private String symbolName;
    private String fixes;

    private DynLibSymbol(String alias, String symbolName) {
      this.alias = alias;
      this.symbolName = symbolName;
    }

    private DynLibSymbol(String symbolName) {
      this.symbolName = symbolName;
      this.alias = symbolName;
    }

    public String getAlias() {
      return alias;
    }

    public String getSymbolName() {
      return symbolName;
    }
  }

  public static class PackageImportEntry {
    private String packageName;
    private boolean allSymbols;
    private List<Symbol> symbols = Lists.newArrayList();
    private List<String> classes = Lists.newArrayList();
    private List<String> methods = Lists.newArrayList();

    public PackageImportEntry(String packageName) {
      this.packageName = packageName;
    }

    /**
     * @return the name of the package whose namespace should be imported.
     */
    public String getPackageName() {
      return packageName;
    }

    /**
     *
     * @return true if all exported symbols from this package should be imported
     */
    public boolean isAllSymbols() {
      return allSymbols;
    }

    /**
     *
     * @return the list of exported symbols that should be imported from this package's namespace.
     */
    public List<Symbol> getSymbols() {
      return symbols;
    }

    /**
     *
     * @return the list of S4 classes that should be imported from this package's namespace.
     */
    public List<String> getClasses() {
      return classes;
    }

    /**
     *
     * @return the list of S4 methods that should be imported from this package's namespace.
     */
    public List<String> getMethods() {
      return methods;
    }
  }

  public static class JvmClassImportEntry {
    private String className;
    private boolean classImported;
    private Set<String> methods = new HashSet<String>();

    public JvmClassImportEntry(String className) {
      this.className = className;
    }

    /**
     * @return the fully-qualified name of the JVM class that should be imported. For example, "java.util.HashMap"
     */
    public String getClassName() {
      return className;
    }

    /**
     *
     * @return true if the class itself should be imported as {@code Class.getSimpleName()}
     */
    public boolean isClassImported() {
      return classImported;
    }

    /**
     *
     * @return the set of static methods that should be imported as symbols from this class.
     */
    public Set<String> getMethods() {
      return methods;
    }
  }

  public static class S3MethodEntry {
    private String genericMethod;
    private String className;
    private String functionName;

    private S3MethodEntry(String genericMethod, String className) {
      this.genericMethod = genericMethod;
      this.className = className;
      this.functionName = genericMethod + "." + className;
    }

    private S3MethodEntry(String genericMethod, String className, String functionName) {
      this.genericMethod = genericMethod;
      this.className = className;
      this.functionName = functionName;
    }

    /**
     *
     * @return the name of the generic method (for example, "print" or "predict")
     */
    public String getGenericMethod() {
      return genericMethod;
    }

    /**
     *
     * @return the S3 class to which this exported function applies.
     */
    public String getClassName() {
      return className;
    }

    /**
     * @return the name of the function that provides the implementation
     */
    public String getFunctionName() {
      return functionName;
    }
  }

  private Map<String, PackageImportEntry> packageImports = Maps.newHashMap();
  private Map<String, JvmClassImportEntry> jvmImports = Maps.newHashMap();
  private List<DynLibEntry> dynLibEntries = Lists.newArrayList();

  private Set<String> exportedPatterns = Sets.newHashSet();
  private Set<Symbol> exportedSymbols = Sets.newHashSet();
  private List<S3MethodEntry> exportedS3Methods = Lists.newArrayList();
  private List<String> exportedS4Methods = Lists.newArrayList();
  private Set<String> exportedClasses = Sets.newHashSet();
  private Set<String> exportedClassPatterns = Sets.newHashSet();

  public static NamespaceFile parse(Context context, CharSource charSource) throws IOException {
    NamespaceFile file = new NamespaceFile();
    ExpressionVector source;
    try(Reader reader = charSource.openStream()) {
      source = RParser.parseAllSource(reader);
    }
    file.parse(context, source);
    return file;
  }

  private NamespaceFile() {
  }

  private PackageImportEntry packageImport(String packageName) {
    PackageImportEntry entry = packageImports.get(packageName);
    if(entry == null) {
      entry = new PackageImportEntry(packageName);
      packageImports.put(packageName, entry);
    }
    return entry;
  }

  private PackageImportEntry packageImport(SEXP argument) {
    return packageImport(parseStringArgument(argument));
  }

  private JvmClassImportEntry classImport(String className) {
    JvmClassImportEntry entry = jvmImports.get(className);
    if(entry == null) {
      entry = new JvmClassImportEntry(className);
      jvmImports.put(className, entry);
    }
    return entry;
  }

  private void parse(Context context, ExpressionVector source) {
    for(SEXP exp : source) {
      parse(context, exp);
    }
  }

  private void parse(Context context, SEXP exp) {

    if(exp instanceof ExpressionVector) {
      parse(context, (ExpressionVector) exp);

    } else if(exp instanceof FunctionCall) {
      FunctionCall call = (FunctionCall)exp;
      parseCall(context, call);

    } else {
      throw new EvalException("Unknown NAMESPACE directive: " + exp.toString());
    }
  }

  private void parseCall(Context context, FunctionCall call) {
    String directiveName = parseDirectiveName(call);
    if(directiveName.equals("import")) {
      parseImport(call);
    } else if(directiveName.equals("importClass") ||
        directiveName.equals("importClasses")) {
      parseImportClass(call);
    } else if(directiveName.equals("importFrom")) {
      parseImportFrom(call);
    } else if(directiveName.equals("importFromClass")) {
      parseImportFromClass(call);
    } else if(
        directiveName.equals("importClassFrom") ||
        directiveName.equals("importClassesFrom")) {
      parseImportS4ClassesFrom(call);
    } else if(directiveName.equals("importMethodsFrom")) {
      parseImportS4MethodsFrom(call);
    } else if(directiveName.equals("S3method")) {
      parseS3Export(call);
    } else if(directiveName.equals("export") ||
        directiveName.equals("exports")) {
      parseExport(call);
    } else if(directiveName.equals("exportPattern") ||
        directiveName.equals("exportPatterns")) {
      parseExportPattern(call);
    } else if(directiveName.equals("useDynLib")) {
      parseDynlib(call);
    } else if(directiveName.equals("exportClasses") ||
        directiveName.equals("exportClass")) {
      parseExportClasses(call);
    } else if(directiveName.equals("exportClassPattern") ||
        directiveName.equals("exportClassPatterns")) {
      parseExportClassPatterns(call);
    } else if(directiveName.equals("exportMethods") ||
        directiveName.equals("exportMethod")) {
      parseExportMethods(call);
    } else if(directiveName.equals("if")) {
      parseIf(context, call);

    } else if(directiveName.equals("{")) {
      for (SEXP sexp : call.getArguments().values()) {
        parse(context, sexp);
      }
    } else {
      throw new EvalException("Unknown NAMESPACE directive '" + directiveName + "'");
    }
  }

  private void parseIf(Context context, FunctionCall call) {
    // evaluate if statements in the context of the base package
    SEXP condition = call.getArgument(0);

    SEXP evaluatedCondition = context.evaluate(condition, context.getBaseEnvironment());

    if(isTruthy(evaluatedCondition)) {
      parse(context, call.getArgument(1));
    } else {
      if(call.getArguments().length() == 3) {
        parse(context, call.getArgument(2));
      }
    }
  }

  private boolean isTruthy(SEXP s) {
    if (s.length() == 0) {
      throw new EvalException("argument is of length zero");
    }

    Logical logical = s.asLogical();
    if (logical == Logical.NA) {
      throw new EvalException("missing value where TRUE/FALSE needed");
    }

    return logical == Logical.TRUE;

  }

  /**
   * Parses the {@code exportPattern(pattern1, pattern2, ...)} directive, where {@code pattern} is a 
   * regular expression used to match symbols defined in the namespace environment that
   * are to be exported.
   */
  private void parseExportPattern(FunctionCall call) {
    exportedPatterns.addAll(parseNameArguments(call));
  }

  /**
   * Parses the {@code export(symbol1, symbol2, ....)} directive, where each argument
   * is a specific symbol to exported from the namespace environment.
   */
  private void parseExport(FunctionCall call) {
    for(SEXP argument : call.getArguments().values()) {
      exportedSymbols.add(parseSymbolArgument(argument));
    }
  }

  /**
   * Parses the {@code import(packageName1, packageName2, ... )} directive, where each argument
   * specifies a package from which all symbols should be imported.
   *
   */
  private void parseImport(FunctionCall call) {
    for (String packageName : parseNameArguments(call)) {
      packageImport(packageName).allSymbols = true;
    }
  }

  /**
   * Parses the {@code importFrom(packageName, symbol1, symbol2, ...)} directive, where 
   * each of the {@code symbol} arguments specifies a symbol to be imported from {@code packageName}.
   *
   * <p>This allows only a specific needed symbols to be imported rather than an entire package namespace.</p>
   */
  private void parseImportFrom(FunctionCall call) {
    if(call.getArguments().length() < 1) {
      throw new EvalException("Expected at least one arguments to importFrom directive");
    }
    PackageImportEntry packageImport = packageImport(call.getArgument(0));

    for(int i=1;i<call.getArguments().length();++i) {
      packageImport.symbols.add(parseSymbolArgument(call.getArgument(i)));
    }
  }

  /**
   * Parses the {@code importClassesFrom(packageName, class1, class2, ...)} directive, which
   * imports each of the given S4 classes from the given {@code packageName}.
   *
   */
  private void parseImportS4ClassesFrom(FunctionCall call) {
    if(call.getArguments().length() < 2) {
      throw new EvalException("Expected at least two arguments to importClassesFrom directive");
    }

    PackageImportEntry packageImport = packageImport(call.getArgument(0));

    for(int i=1;i<call.getArguments().length();++i) {
      packageImport.classes.add(parseStringArgument(call.getArgument(i)));
    }
  }

  /**
   * Parses the {@code importMethodsFrom(packageName, class1, class2, ...)} directive, which
   * imports each of the given S4 methods from the given {@code packageName}.
   *
   */
  private void parseImportS4MethodsFrom(FunctionCall call) {
    if(call.getArguments().length() < 2) {
      throw new EvalException("Expected at least two arguments to importMethodsFrom directive");
    }

    PackageImportEntry packageImport = packageImport(call.getArgument(0));

    for(int i=1;i<call.getArguments().length();++i) {
      packageImport.methods.add(parseStringArgument(call.getArgument(i)));
    }
  }

  /**
   * Parses the Renjin-specific {@code importClass(class1, calss2, class3)} directive, which imports each of the given
   * JVM classes as symbols into the namespace's imports.
   *
   * <p>For example, {@code importClass(java.util.HashMap} will add the binding {@code HashMap} to the namespace's
   * imports environment, so that a package function could invoke {@code HashMap$new()}</p>
   */
  private void parseImportClass(FunctionCall call) {
    for (String className : parseNameArguments(call)) {
      classImport(className).classImported = true;
    }
  }

  /**
   * Parses the Renjin-specific {@code importFromClass(className, methodName1, methodName2, ...)}, which imports each
   * of the static methods referenced by {@code methodName1}, {@code methodName2} from the JVM class {@code className}.
   *
   * <p>For example, {@code importFromClass(java.lang.System, nanoTime} will import the symbol {@code nanoTime}, so
   * that a package function could invoke {@code x <- nanoTime()}</p>
   */
  private void parseImportFromClass(FunctionCall call) {
    if(call.getArguments().length() < 2) {
      throw new EvalException("Expected at least two arguments to importFromClass directive");
    }
    String className = parseStringArgument(call.getArgument(0));
    JvmClassImportEntry entry = classImport(className);
    for(int i=1;i<call.getArguments().length();++i) {
      String methodName = parseStringArgument(call.getArgument(i));
      entry.methods.add(methodName);
    }
  }

  private static List<String> parseNameArguments(FunctionCall call) {
    List<String> names = Lists.newArrayList();
    for(SEXP argument : call.getArguments().values()) {
      names.add(parseStringArgument(argument));
    }
    return names;
  }


  private static Symbol parseSymbolArgument(SEXP argument) {
    if(argument instanceof Symbol) {
      return (Symbol) argument;
    } else if(argument instanceof StringVector && argument.length() == 1) {
      return Symbol.get(((StringVector) argument).getElementAsString(0));
    } else {
      throw new EvalException("Can't parse directive argument '" + argument + "' as symbol");
    }
  }

  private static String parseStringArgument(SEXP argument) {
    if(argument instanceof StringVector && argument.length() == 1) {
      return ((StringVector) argument).getElementAsString(0);
    } else if(argument instanceof Symbol) {
      return ((Symbol) argument).getPrintName();
    } else {
      throw new EvalException("Can't parse directive argument '" + argument + "' as string");
    }
  }

  private static String parseDirectiveName(FunctionCall call) {
    if(call.getFunction() instanceof Symbol) {
      return ((Symbol) call.getFunction()).getPrintName();
    } else {
      throw new EvalException("Unknown NAMESPACE directive: " + call);
    }
  }

  private void parseS3Export(FunctionCall call) {
    if(call.getArguments().length() == 2) {
      if(call.getArgument(1) == Null.INSTANCE) {
        // S3method(functionName, NULL)
        // just exports the generic function?
        exportedSymbols.add(parseSymbolArgument(call.getArgument(0)));

      } else {
        // S3method(functionName, 
        exportedS3Methods.add(new S3MethodEntry(
            parseStringArgument(call.getArgument(0)),
            parseStringArgument(call.getArgument(1))));
      }
    } else if(call.getArguments().length() == 3) {
      exportedS3Methods.add(new S3MethodEntry(
          parseStringArgument(call.getArgument(0)),
          parseStringArgument(call.getArgument(1)),
          parseStringArgument(call.getArgument(2))));
    } else {
      throw new UnsupportedOperationException("Expected 2 or 3 arguments to S3Method directive");
    }
  }

  private void parseExportClasses(FunctionCall call) {
    exportedClasses.addAll(parseNameArguments(call));
  }

  private void parseExportClassPatterns(FunctionCall call) {
    exportedClassPatterns.addAll(parseNameArguments(call));
  }


  private void parseExportMethods(FunctionCall call) {
    exportedS4Methods.addAll(parseNameArguments(call));
  }


  private void parseDynlib(FunctionCall call) {
    if(call.getArguments().length() < 1) {
      throw new EvalException("Expected at least one argument to useDynlib");
    }

    DynLibEntry entry = new DynLibEntry();
    entry.libraryName = parseStringArgument(call.getArgument(0));

    for(PairList.Node node : Iterables.skip(call.getArguments().nodes(),1)) {
      if(node.hasTag()) {

        if (node.getTag().getPrintName().equals(".registration")) {
          entry.registration = parseLogical(node.getValue());
        } else if (node.getTag().getPrintName().equals(".fixes")) {
          entry.prefix = parseStringArgument(node.getValue());
        } else {

          entry.symbols.add(new DynLibSymbol(
              parseStringArgument(node.getTag()),
              parseStringArgument(node.getValue())));
        }

      } else {
        entry.symbols.add(new DynLibSymbol(
            parseStringArgument(node.getValue())));
      }
    }
    dynLibEntries.add(entry);
  }

  private static boolean parseLogical(SEXP value) {
    if(value instanceof LogicalVector && value.length() == 1) {
      return ((LogicalVector) value).getElementAsRawLogical(0) == 1;
    } else {
      throw new EvalException("Expected TRUE or FALSE");
    }
  }

  /**
   *
   * @return the collection of packages from which this namespace imports.
   */
  public Collection<PackageImportEntry> getPackageImports() {
    return packageImports.values();
  }

  /**
   *
   * @return the collection of JVM classes from which this namespace imports.
   */
  public Collection<JvmClassImportEntry> getJvmImports() {
    return jvmImports.values();
  }

  /**
   *
   * Gets the list of dynamic libraries from which this namespace imports.
   *
   * <p>In GNU R, this directive refers to native libraries compiled from C/C++/Fortran. Renjin
   * has a tool chain which will compile these sources to a JVM class. This directive is retained
   * for compatability with GNU R packages.</p>
   *
   * @return the list of dynamic libraries from which this namespace imports. 
   */
  public List<DynLibEntry> getDynLibEntries() {
    return dynLibEntries;
  }

  /**
   *
   * @return the set of patterns that are used to match symbols that should be exported from this namespace
   */
  public Set<String> getExportedPatterns() {
    return exportedPatterns;
  }

  /**
   *
   * @return the set of symbols that should be exported from this namespace
   */
  public Set<Symbol> getExportedSymbols() {
    return exportedSymbols;
  }

  /**
   *
   * @return the list of S3 methods in this namespace extending generic functions originally 
   * defined in other namespaces.
   */
  public List<S3MethodEntry> getExportedS3Methods() {
    return exportedS3Methods;
  }

  /**
   *
   * @return the set of S4 classes to be exported from this namespace
   */
  public Set<String> getExportedClasses() {
    return exportedClasses;
  }

  /**
   *
   * @return the set of regular expressions patterns that match the class names which should be exported
   * from this namespace.
   */
  public Set<String> getExportedClassPatterns() {
    return exportedClassPatterns;
  }

  /**
   *
   * @return the set of S4 methods which should be exported from this namespace.
   */
  public List<String> getExportedS4Methods() {
    return exportedS4Methods;
  }
}
