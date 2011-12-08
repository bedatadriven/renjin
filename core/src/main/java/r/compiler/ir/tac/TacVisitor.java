package r.compiler.ir.tac;

import r.compiler.ir.tac.instructions.Assignment;

public interface TacVisitor {

  public void visitAssignment(Assignment assignment);
  
}
