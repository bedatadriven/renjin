package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.gimple.GimpleOp;


public interface PointerTypeStrategy<ExprT extends Expr> extends TypeStrategy<ExprT> {
  
  ExprT malloc(MethodGenerator mv, SimpleExpr sizeInBytes);

  ExprT realloc(ExprT pointer, SimpleExpr newSizeInBytes);

  ExprT pointerPlus(ExprT pointer, SimpleExpr offsetInBytes);

  Expr valueOf(ExprT pointerExpr);

  ExprT nullPointer();

  ConditionGenerator comparePointers(GimpleOp op, ExprT x, ExprT y);

  SimpleExpr memoryCompare(ExprT p1, ExprT p2, SimpleExpr n);

  void memoryCopy(MethodGenerator mv, ExprT destination, ExprT source, SimpleExpr length);

  void memorySet(MethodGenerator mv, ExprT pointer, SimpleExpr byteValue, SimpleExpr length);

  SimpleExpr toVoidPointer(ExprT ptrExpr);
  
  ExprT fromVoidPointer(SimpleExpr ptrExpr);
}
