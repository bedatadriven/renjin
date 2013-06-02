package org.renjin.gcc.translate.type;

import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.Variable;

/**
 * An intermediate representation of a type used during translation
 */
public interface ImType {

  JimpleType paramType();

  JimpleType returnType();

  Variable createLocalVariable(
      FunctionContext functionContext,
      String gimpleName,
      VarUsage varUsage);

  ImType pointerType();

  ImType arrayType(Integer lowerBound, Integer upperBound);
}
