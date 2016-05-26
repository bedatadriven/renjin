package org.renjin.gcc.symbols;


import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;

public interface SymbolTable {

  SimpleExpr findHandle(GimpleFunctionRef ref);

  CallGenerator findCallGenerator(GimpleFunctionRef ref);

  Expr getVariable(GimpleSymbolRef ref);

}
