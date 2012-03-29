package org.renjin.compiler.ir.tac.statements;

public interface StatementVisitor {

  void visitAssignment(Assignment assign);
  void visitExprStatement(ExprStatement statement);
  void visitGoto(GotoStatement statement);
  void visitIf(IfStatement ifStatement);
  void visitReturn(ReturnStatement returnStatement);
  
  
}
