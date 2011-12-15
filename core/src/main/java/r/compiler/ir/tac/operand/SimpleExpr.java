package r.compiler.ir.tac.operand;

public interface SimpleExpr extends Operand {

  SimpleExpr renameVariable(Variable name, Variable newName);
}
