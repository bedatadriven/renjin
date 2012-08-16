package org.renjin.gcc.jimple;

import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Simple wrapper for PrintWriter that handles indentation
 */
public class JimpleWriter {

	private PrintWriter writer;
	private int indent = 0;

  public JimpleWriter(File source) throws FileNotFoundException {
    this.writer = new PrintWriter(source);
	}
	
	public void println(String line) {
    if(!line.endsWith(":")) {
      for(int i=0;i!=indent;++i) {
        writer.print("  ");
      }
    }
		writer.println(line);
	}
	
	public void println() {
		writer.println();
	}

  public void startBlock() {
    println("{");
    indent++;
  }
	
	public void closeBlock() {
		indent--;
		println("}");
	}

  public void closeBlockWithSemicolon() {
		indent--;
		println("};");
	}

  public void close() {
		writer.close();
	}

  private void initializeParameters(GimpleFunction function) {
    int paramIndex = 0;
    for(GimpleParameter param : function.getParameters()) {
      println(param.getName() + " := @parameter" + paramIndex + ": " + Jimple.type(param.getType())
          + ";");
      paramIndex++;
    }
  }

  private String argumentList(GimpleFunction function) {
    StringBuilder sb = new StringBuilder();
    boolean needsComma = false;
    for(GimpleParameter param : function.getParameters()) {
      if(needsComma) {
        sb.append(",");
      }
      sb.append(Jimple.type(param.getType()));
      needsComma = true;
    }
    return sb.toString();
  }
}
