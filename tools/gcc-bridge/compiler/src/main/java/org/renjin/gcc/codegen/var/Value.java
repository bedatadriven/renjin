package org.renjin.gcc.codegen.var;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;


public interface Value extends ExprGenerator {

  Type getType();

  void load(MethodGenerator mv);
}
