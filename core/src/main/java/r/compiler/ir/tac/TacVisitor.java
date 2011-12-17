package r.compiler.ir.tac;

import r.compiler.ir.tac.statements.Assignment;

public interface TacVisitor {

  public void visitAssignment(Assignment assignment);
  
}
