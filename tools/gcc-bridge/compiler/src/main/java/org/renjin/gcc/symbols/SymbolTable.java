package org.renjin.gcc.symbols;


import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;

public interface SymbolTable {

  JExpr findHandle(GimpleFunctionRef ref);

  CallGenerator findCallGenerator(GimpleFunctionRef ref);

  GExpr getVariable(GimpleSymbolRef ref);

}
