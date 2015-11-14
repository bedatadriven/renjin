package org.renjin.gcc.symbols;


import org.objectweb.asm.Handle;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.SymbolRef;

public interface SymbolTable {

  Handle findHandle(GimpleFunctionRef ref, CallingConvention callingConvention);

  CallGenerator findCallGenerator(GimpleFunctionRef ref, CallingConvention callingConvention);

  ExprGenerator getVariable(SymbolRef ref);

}
