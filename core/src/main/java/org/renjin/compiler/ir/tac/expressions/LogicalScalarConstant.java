package org.renjin.compiler.ir.tac.expressions;


import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.sexp.Logical;

public class LogicalScalarConstant extends Constant {

  public static final LogicalScalarConstant TRUE = new LogicalScalarConstant(Logical.TRUE);
  public static final LogicalScalarConstant FALSE = new LogicalScalarConstant(Logical.FALSE);
  public static final LogicalScalarConstant NA = new LogicalScalarConstant(Logical.NA);


  private final Logical value;

  private LogicalScalarConstant(Logical value) {
    this.value = value;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

}
