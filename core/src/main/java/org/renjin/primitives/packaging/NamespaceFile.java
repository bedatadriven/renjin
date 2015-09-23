package org.renjin.primitives.packaging;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.google.common.io.Closeables;
import org.renjin.eval.EvalException;
import org.renjin.parser.RParser;
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


  public static class DynlibEntry {
    private String alias;
    private String symbolName;
    private String fixes;

    private DynlibEntry(String alias, String symbolName, String fixes, boolean registration) {
      this.alias = alias;
      this.symbolName = symbolName;
      this.fixes = fixes;
    }

    DynlibEntry(String symbolName, String fixes, boolean registration) {
      this.symbolName = symbolName;
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

    public PackageImportEntry(String packageName) {
      this.packageName = packageName;
    }

    public String getPackageName() {
      return packageName;
    }

    public boolean isAllSymbols() {
      return allSymbols;
    }

    public List<Symbol> getSymbols() {
      return symbols;
    }

    public List<String> getClasses() {
      return classes;
    }
  }

  public static class JvmClassImportEntry {
    private String className;
    private boolean classImported;
    private Set<String> methods = new HashSet<String>();

    private JvmClassImportEntry(String className) {
      this.className = className;
    }

    public String getClassName() {
      return className;
    }

    public boolean isClassImported() {
      return classImported;
    }

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
     * @return the S3 class to which this exported function applies
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
  private Map<String, JvmClassImportEntry> classImports = Maps.newHashMap();
  private List<DynlibEntry> dynLibEntries = Lists.newArrayList();

  private Set<String> exportedPatterns = Sets.newHashSet();
  private Set<Symbol> exportedSymbols = Sets.newHashSet();
  private List<S3MethodEntry> exportedS3Methods = Lists.newArrayList();
  private List<String> exportedS4Methods = Lists.newArrayList();
  private Set<String> exportedClasses = Sets.newHashSet();
  private Set<String> exportedClassPatterns = Sets.newHashSet();

  public NamespaceFile(CharSource charSource) throws IOException {
    Reader reader = charSource.openStream();
    ExpressionVector source;
    try {
      source = RParser.parseAllSource(reader);
    } finally {
      Closeables.closeQuietly(reader);
    }
    parse(source);
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
    JvmClassImportEntry entry = classImports.get(className);
    if(entry == null) {
      entry = new JvmClassImportEntry(className);
    }
    return entry;
  }

  private void parse(ExpressionVector source) {
    for(SEXP exp : source) {
      if(!(exp instanceof FunctionCall)) {
        throw new EvalException("Unknown NAMESPACE directive: " + exp.toString());
      }

      FunctionCall call = (FunctionCall)exp;
      parseCall(call);
    }
  }

  private void parseCall(FunctionCall call) {
    String directiveName = parseDirectiveName(call);
    if(directiveName.equals("import")) {
      parseImport(call);
    } else if(directiveName.equals("importClass")) {
      parseImportClass(call);
    } else if(directiveName.equals("importFrom")) {
      parseImportFrom(call);
    } else if(directiveName.equals("importFromClass")) {
      parseImportFromClass(call);
    } else if(directiveName.equals("importClassesFrom")) {
      parseImportS4ClassesFrom(call);
    } else if(directiveName.equals("S3method")) {
      parseS3Export(call);
    } else if(directiveName.equals("export")) {
      parseExport(call);
    } else if(directiveName.equals("exportPattern")) {
      parseExportPattern(call);
    } else if(directiveName.equals("useDynLib")) {
      parseDynlib(call);
    } else if(directiveName.equals("exportClasses")) {
      parseExportClasses(call);
    } else if(directiveName.equals("exportClassPatterns")) {
      parseExportClassPatterns(call);
    } else if(directiveName.equals("exportMethods")) {
      parseExportMethods(call);
    } else {
      throw new EvalException("Unknown NAMESPACE directive '" + directiveName + "'");
    }
  }

  private void parseExportPattern(FunctionCall call) {
    if(call.getArguments().length() != 1) {
      throw new EvalException("Expected one argument to exportPattern() directive");
    }
    exportedPatterns.add(parseStringArgument(call.getArgument(0)));
  }

  private void parseExport(FunctionCall call) {
    for(SEXP argument : call.getArguments().values()) {
      exportedSymbols.add(parseSymbolArgument(argument));
    }
  }

  private void parseImport(FunctionCall call) {
    for (String packageName : parseNameArguments(call)) {
      packageImport(packageName).allSymbols = true;
    }
  }

  private void parseImportFrom(FunctionCall call) {
    if(call.getArguments().length() < 2) {
      throw new EvalException("Expected at least two arguments to importFrom directive");
    }
    PackageImportEntry packageImport = packageImport(call.getArgument(0));

    for(int i=1;i<call.getArguments().length();++i) {
      packageImport.symbols.add(parseSymbolArgument(call.getArgument(i)));
    }
  }

  private void parseImportS4ClassesFrom(FunctionCall call) {
    if(call.getArguments().length() < 2) {
      throw new EvalException("Expected at least two arguments to importClassesFrom directive");
    }

    PackageImportEntry packageImport = packageImport(call.getArgument(0));

    for(int i=1;i<call.getArguments().length();++i) {
      packageImport.classes.add(parseStringArgument(call.getArgument(i)));
    }
  }

  private void parseImportClass(FunctionCall call) {
    for (String className : parseNameArguments(call)) {
      classImport(className).classImported = true;
    }
  }

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
      exportedS3Methods.add(new S3MethodEntry(
          parseStringArgument(call.getArgument(0)),
          parseStringArgument(call.getArgument(1))));
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
    String libName = parseStringArgument(call.getArgument(0));

    boolean registration = false;
    String fixes = "";

    for(PairList.Node node : Iterables.skip(call.getArguments().nodes(),1)) {
      if(node.hasTag()) {
        if (node.getTag().getPrintName().equals(".registration")) {
          registration = parseLogical(node.getValue());
        } else if (node.getTag().getPrintName().equals(".fixes")) {
          fixes = parseStringArgument(node.getValue());
        }
      }
    }

    for(PairList.Node node : Iterables.skip(call.getArguments().nodes(),1)) {
      if(node.hasTag()) {
        if(!node.getTag().getPrintName().equals(".registration") && 
           !node.getTag().getPrintName().equals(".fixes")) {

          dynLibEntries.add(new DynlibEntry(
                  parseStringArgument(node.getTag()),
                  parseStringArgument(node.getValue()), fixes, registration));
        }
      } else {
        dynLibEntries.add(new DynlibEntry(
                parseStringArgument(node.getValue()), fixes, registration));
      }
    }
  }

  private static boolean parseLogical(SEXP value) {
    if(value instanceof LogicalVector && value.length() == 1) {
      return ((LogicalVector) value).getElementAsRawLogical(0) == 1;
    } else {
      throw new EvalException("Expected TRUE or FALSE");
    }
  }

  public Collection<PackageImportEntry> getPackageImports() {
    return packageImports.values();
  }

  public Collection<JvmClassImportEntry> getClassImports() {
    return classImports.values();
  }

  public List<DynlibEntry> getDynLibEntries() {
    return dynLibEntries;
  }

  public Set<String> getExportedPatterns() {
    return exportedPatterns;
  }

  public Set<Symbol> getExportedSymbols() {
    return exportedSymbols;
  }

  public List<S3MethodEntry> getExportedS3Methods() {
    return exportedS3Methods;
  }

  public Set<String> getExportedClasses() {
    return exportedClasses;
  }

  public Set<String> getExportedClassPatterns() {
    return exportedClassPatterns;
  }

  public List<String> getExportedS4Methods() {
    return exportedS4Methods;
  }
}
