package org.renjin.gcc.translate;


import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleType;

/**
 * Handles translation of types between Gimple and Jimple
 */
public abstract class TypeTranslator {

  public abstract void declareParameter(JimpleMethodBuilder builder, GimpleParameter param);

  public abstract JimpleType paramType();

  public abstract JimpleType returnType();

  public abstract void declareVariable(JimpleMethodBuilder builder, GimpleVarDecl varDecl);
}
