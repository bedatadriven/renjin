package r.compiler.ir.tac;

import java.io.IOException;

import r.lang.Context;
import r.lang.SEXP;

public class IRProgram {

  private IRBlock block;
  private IRFunctionTable functionTable = new IRFunctionTable();
  
  public IRProgram(SEXP program) {
    IRBlockBuilder builder = new IRBlockBuilder(functionTable);
    block = builder.build(program);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(IRFunction function : functionTable) {
      sb.append("function@" + function.getId()).append("\n");
      sb.append(function.getBlock()).append("\n");
    }
    
    sb.append("\nMAIN:\n");
    sb.append(block.toString());
    return sb.toString();
  }
  
  public SEXP evaluate() throws IOException {
    Context context = Context.newTopLevelContext();
    context.init();
    return block.evaluate(context);
  }
  
  
}
