package org.renjin.gcc.translate.field;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.type.struct.SimpleRecordArrayType;

public class SimpleRecordArrayFieldExpr extends AbstractImExpr {

  private final String instanceName;
  private final JimpleType declaringClass;
  private final String member;
  private final SimpleRecordArrayType memberType;

  public SimpleRecordArrayFieldExpr(SimpleRecordArrayType memberType, String member,
                                    JimpleType declaringClass, String instanceName) {
    this.memberType = memberType;
    this.member = member;
    this.declaringClass = declaringClass;
    this.instanceName = instanceName;
  }


  @Override
  public ImType type() {
    return memberType;
  }

  private JimpleExpr fieldRef() {
    return new JimpleExpr(String.format("<%s: %s %s>",
        declaringClass, memberType.jimpleType().toString(), member));
  }



  @Override
  public ImExpr addressOf() {
    return new Pointer();
  }

  public class Pointer extends AbstractImExpr {

    @Override
    public ImType type() {
      return memberType.pointerType();
    }

    @Override
    public JimpleExpr translateToObjectReference(FunctionContext context, JimpleType className) {
      if(className.is(Object.class)) {
        if(instanceName == null) {
          return fieldRef();
        } else {
          throw new UnsupportedOperationException("non static not yet supported");
        }
      }
      return super.translateToObjectReference(context, className);
    }
  }


}
