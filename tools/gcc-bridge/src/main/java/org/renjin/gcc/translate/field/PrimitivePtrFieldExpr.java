package org.renjin.gcc.translate.field;

import com.google.common.base.Preconditions;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.*;
import org.renjin.gcc.translate.type.ImPrimitivePtrType;

/**
 * An intermediate expression bound to a field on a JVM class
 */
public class PrimitivePtrFieldExpr extends AbstractImExpr implements ImIndirectExpr, ImLValue {


  /**
   * The instance name to which this expression refers
   */
  private String instanceName;

  /**
   * The class which declares the field to which this expression is bound
   */
  private JimpleType declaringClass;

  /**
   * The name of the public field to which this expression is bound
   */
  private String member;
  private ImPrimitivePtrType memberType;

  private JimpleType arrayType;

  public PrimitivePtrFieldExpr(String instanceName, JimpleType declaringClass,
                            String member, ImPrimitivePtrType memberType) {
    this.instanceName = instanceName;
    this.declaringClass = declaringClass;
    this.member = member;
    this.memberType = memberType;
    this.arrayType = memberType.baseType().jimpleArrayType();


    Preconditions.checkNotNull("memberType", this.memberType);
  }

  @Override
  public ArrayRef translateToArrayRef(FunctionContext context) {
    String arrayVar = context.getBuilder().addTempVarDecl(memberType.baseType().jimpleArrayType());
    String indexVar = context.getBuilder().addTempVarDecl(JimpleType.INT);
    
    context.getBuilder().addAssignment(arrayVar, arrayMemberExpr());
    context.getBuilder().addAssignment(indexVar, indexMemberExpr());


    return new ArrayRef(arrayVar, indexVar);
  }

  private JimpleExpr indexMemberExpr() {
    return memberExpr(JimpleType.INT, memberType.indexMemberName(member));
  }

  private JimpleExpr arrayMemberExpr() {
    return memberExpr(arrayType, member);
  }


  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
    if(rhs instanceof ImIndirectExpr) {
      ArrayRef ref = ((ImIndirectExpr) rhs).translateToArrayRef(context);
      context.getBuilder().addAssignment(arrayMemberExpr(), ref.getArrayExpr());
      context.getBuilder().addAssignment(indexMemberExpr(), ref.getIndexExpr());
    } else {
      throw new UnsupportedOperationException(rhs.toString());
    }
  }

  @Override
  public ImPrimitivePtrType type() {
    return memberType;
  }
  
  private JimpleExpr memberExpr(JimpleType memberType, String name) {
    String signature = String.format("<%s: %s %s>", declaringClass, memberType, name);

    if(instanceName == null) {
      return new JimpleExpr(signature);
    } else {
      return new JimpleExpr(instanceName + "." + signature);
    }
  }


  @Override
  public String toString() {
    return declaringClass + "." + member;
  }

}
