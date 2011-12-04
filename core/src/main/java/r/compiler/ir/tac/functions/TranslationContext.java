package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.instructions.Statement;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.SimpleExpr;
import r.compiler.ir.tac.operand.Temp;
import r.lang.SEXP;

public interface TranslationContext {
  Label newLabel();
  Temp newTemp();
  void translateStatements(SEXP exp);
  Operand translateExpression(SEXP exp);
  SimpleExpr translateSimpleExpression(SEXP exp);
  void addStatement(Statement statement);
  void addLabel(Label label);
}
