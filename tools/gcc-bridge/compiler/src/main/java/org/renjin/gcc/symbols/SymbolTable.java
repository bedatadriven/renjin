package org.renjin.gcc.symbols;


import java.util.List;

import org.objectweb.asm.Handle;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;

public interface SymbolTable {

  Handle findHandle(GimpleFunctionRef ref);

  CallGenerator findCallGenerator(GimpleFunctionRef ref, List<GimpleExpr> operands);

  Expr getVariable(GimpleSymbolRef ref);

}
