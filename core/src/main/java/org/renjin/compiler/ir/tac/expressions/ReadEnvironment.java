package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Map;

/**
 * Reads the initial value of a variable from the R environment
 */
public class ReadEnvironment implements Expression {

  private Symbol name;
  private ValueBounds valueBounds;

  public ReadEnvironment(Symbol name, ValueBounds valueBounds) {
    this.name = name;
    this.valueBounds = valueBounds;
  }

  @Override
  public boolean isDefinitelyPure() {
    return false;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    mv.visitVarInsn(Opcodes.ALOAD, emitContext.getEnvironmentVarIndex());
    mv.visitLdcInsn(name.getPrintName());
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Symbol.class), "get", 
        Type.getMethodDescriptor(Type.getType(Symbol.class), Type.getType(String.class)), false);
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Environment.class), "findVariable", 
        Type.getMethodDescriptor(Type.getType(SEXP.class), Type.getType(Symbol.class)), false);
    
    return 2;
  }

  @Override
  public Type getType() {
    return Type.getType(SEXP.class);
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    return "read(" + name + " = " + valueBounds + ")";
  }
}
