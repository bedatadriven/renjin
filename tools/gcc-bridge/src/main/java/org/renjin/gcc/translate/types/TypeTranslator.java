package org.renjin.gcc.translate.types;


import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.var.Variable;

/**
 * Handles translation of types between Gimple and Jimple
 */
public abstract class TypeTranslator {

  public abstract JimpleType paramType();

  public abstract JimpleType returnType();

  public abstract Variable createLocalVariable(FunctionContext functionContext, String gimpleName);

}
