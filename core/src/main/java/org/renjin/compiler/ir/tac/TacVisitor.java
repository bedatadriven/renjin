package org.renjin.compiler.ir.tac;

import org.renjin.compiler.ir.tac.statements.Assignment;

public interface TacVisitor {

  public void visitAssignment(Assignment assignment);
  
}
