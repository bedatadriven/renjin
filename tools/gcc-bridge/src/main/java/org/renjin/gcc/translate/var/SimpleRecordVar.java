package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.field.PrimitiveFieldExpr;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.type.struct.SimpleRecordType;

public class SimpleRecordVar extends AbstractImExpr implements Variable {

  private ImPrimitiveType type;
  private SimpleRecordType recordType;
  private String jimpleName;
  private FunctionContext context;

  public SimpleRecordVar(FunctionContext context, String gimpleName, SimpleRecordType struct) {
    this.context = context;
    this.recordType = struct;
    this.jimpleName = Jimple.id(gimpleName);

    context.getBuilder().addVarDecl(struct.getJimpleType(), jimpleName);
    context.getBuilder().addStatement(jimpleName + " = new " + struct.getJimpleType());
    context.getBuilder().addStatement(
        "specialinvoke " + jimpleName + ".<" + struct.getJimpleType() + ": void <init>()>()");
  }

  @Override
  public ImExpr member(String member) {
    return new PrimitiveFieldExpr(jimpleName, recordType.getJimpleType(),
        member, recordType.getMemberType(member) );
  }

  @Override
  public ImPrimitiveType type() {
    return type;
  }

  @Override
  public ImExpr addressOf() {
    return new Pointer();
  }

  public Variable asPtrVariable() {
    return new PointerVariable();
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return jimpleName + ":" + recordType;
  }

  @Override
  public JimpleExpr translateToObjectReference(FunctionContext context, JimpleType className) {
    if(className.equals(recordType.getJimpleType())) {
      return new JimpleExpr(jimpleName);
    } else if(className.is(Object.class)) {
      return new JimpleExpr(jimpleName);
    }
    return super.translateToObjectReference(context, className);
  }

  private class Pointer extends AbstractImExpr {

    @Override
    public ImType type() {
      return SimpleRecordVar.this.type().pointerType();
    }


    @Override
    public ImExpr memref() {
      return SimpleRecordVar.this;
    }

    @Override
    public JimpleExpr translateToObjectReference(FunctionContext context, JimpleType className) {
      return SimpleRecordVar.this.translateToObjectReference(context, className);
    }
  }

  private class PointerVariable extends Pointer implements Variable {

    @Override
    public void writeAssignment(FunctionContext context, ImExpr rhs) {
      context.getBuilder().addAssignment(jimpleName,
          rhs.translateToObjectReference(context,
              recordType.getJimpleType()));
    }
  }
}
