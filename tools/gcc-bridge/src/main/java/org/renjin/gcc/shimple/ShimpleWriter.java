package org.renjin.gcc.shimple;

import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ShimpleWriter {

	private PrintWriter writer;
	private int indent = 0;
	private Object className;
	
	public ShimpleWriter(File source, String className) throws FileNotFoundException {
		this.writer = new PrintWriter(source);
		this.className = className;
		
		println("public class " + className + " extends java.lang.Object");
    startBlock();
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

  private void startBlock() {
    println("{");
    indent++;
  }
	
	public void closeBlock() {
		indent--;
		println("}");
	}
	
	public void writeDefaultConstructor() {
		println("public void <init>()");
    startBlock();
    println(className + " r0;");
    println("r0 := @this: " + className + ";");
		println("specialinvoke r0.<java.lang.Object: void <init>()>();");
    println("return;");
		closeBlock();
	}

  public void close() {
		writer.close();
	}

  public void writeFunction(GimpleFunction function) {
    println("public static " + Shimple.type(function.returnType()) + " " + function.getName() + "(" +
        argumentList(function) + ")");
    startBlock();

    for(GimpleParameter param : function.getParameters()) {
      println(Shimple.type(param.getType()) + " " + Shimple.id(param.getName()) + ";");
    }

    for(GimpleVarDecl var : function.getVariableDeclarations()) {
      println(Shimple.type(var.getType()) + " " + Shimple.id(var.getName()) + ";");
    }

    initializeParameters(function);

    ShimpleInsVisitor insWriter = new ShimpleInsVisitor(this);
    function.visitIns(insWriter);

    closeBlock();
  }

  private void initializeParameters(GimpleFunction function) {
    int paramIndex = 0;
    for(GimpleParameter param : function.getParameters()) {
      println(param.getName() + " := @parameter" + paramIndex + ": " + Shimple.type(param.getType())
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
      sb.append(Shimple.type(param.getType()));
      needsComma = true;
    }
    return sb.toString();
  }
}
