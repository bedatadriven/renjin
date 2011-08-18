package r.jvmi.wrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility class for writing java source for generated wrappers 
 * for functions. 
 * 
 * @author alex
 *
 */
public class WrapperSourceWriter {
  private PrintWriter writer;
  private String className;
  private int indent = 0;
  
  public WrapperSourceWriter(File sourceFile, String className) throws IOException {
    writer = new PrintWriter( sourceFile );
    this.className = className;
  }
  
  public void writePackage(String packageName) {
    println("package " + packageName + ";");
    writeBlankLine();
  }
  
  public void writeImport(String className) {
    println("import " + className + ";");
  }
  
  public void writeStaticImport(String spec) {
    println("import static " + spec + ";");
  }
    
  public void writeBeginClass() {
    println("public class " + className + " extends BuiltinFunction {");
    indent++;
  }
  
  public void writeConstructor(String functionName) {
    println("public " + className + "() { super(" + quote(functionName) + "); }");
  }  
  
  public void writeBeginApplyMethod() {
    println("@Override");
    println("public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList args) {");
    indent++;
  }
  
  public void writeComment(String comment) {
    println("// " + comment);
  }
  
  public void writeStatement(String statement) {
    println(statement);
  }
  

  public void writeStatementF(String statement, Object... args) {
    writeStatement(String.format(statement, args));
  }
  
  public void writeCloseBlock() {
    indent--;
    println("}");
  }
  
  public void writeBlankLine() {
    writer.println();
  }
  
  public void println(String line) {
    for(int i=0;i<indent;++i) {
      writer.print("  ");
    }
    writer.println(line);
  }
  
  public void printlnf(String line, Object... args) {
    println(String.format(line, args));
  }
  
  public void printf(String line, Object... args) {
    writer.print(String.format(line, args));
  }
  

  public String quote(String name) {
    return "\"" + name + "\"";
  }
  
  public void flush() {
    writer.flush();
  }

  public void println() {
    writer.println();
  }

  public void close() {
    writer.close();
  }

  public void writeBeginTry() {
    writeStatement("try {");
    indent ++;
  }
  
  public void writeCatch(Class exceptionClass, String variableName) {
    indent--;
    println("} catch (" + exceptionClass.getName() + " " + variableName + ") { ");
    indent++;
  }

}