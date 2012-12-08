package org.renjin.gcc.translate.struct;

import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.GimpleAssign;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.GimpleStructType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.gimple.type.PrimitiveType;

import java.util.Map;

public class MemberFinder extends GimpleVisitor {

  private GimpleFunction function;
  private String structName;

  private Map<String, GimpleType> members = Maps.newHashMap();

  public MemberFinder(String name) {
    this.structName = name;
  }

  public void visit(GimpleFunction function) {
    this.function = function;
    function.visitIns(this);
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {
    GimpleLValue lhs = assignment.getLHS();
    GimpleExpr rhs = assignment.getOperands().get(0);

    if(assignment.getOperator() == GimpleOp.COMPONENT_REF) {
      if(structName(rhs).equals(structName)) {
        members.put(structMember(rhs), typeFromExpr(lhs));
      }
    } else if(lhs instanceof GimpleCompoundRef) {
      if(structName(lhs).equals(structName)) {
        members.put(structMember(lhs), typeFromExpr(rhs));
      }
    }
  }

  private GimpleType typeFromExpr(GimpleExpr expr) {
    if(expr instanceof GimpleVar) {
      return function.getVariableType(((GimpleVar) expr).getName());
    } else if(expr instanceof GimpleConstant) {
      Object value = ((GimpleConstant) expr).getValue();
      if(value instanceof Double) {
        return PrimitiveType.DOUBLE_TYPE;
      } else if(value instanceof Integer) {
        return PrimitiveType.INT_TYPE;
      }
    }
    throw new IllegalArgumentException(expr.toString());
  }

  private String structName(GimpleExpr expr) {
    if(expr instanceof GimpleCompoundRef) {
      GimpleCompoundRef ref = (GimpleCompoundRef) expr;
      GimpleType type = function.getVariableType(ref.getVar().getName());
      return structName(type);
    }
    throw new UnsupportedOperationException(expr.toString());
  }

  private String structName(GimpleType type) {
    if(type instanceof GimpleStructType) {
      return ((GimpleStructType) type).getName();
    } else if(type instanceof PointerType) {
      return structName(((PointerType) type).getInnerType());
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }


  private String structMember(GimpleExpr expr) {
    if(expr instanceof GimpleCompoundRef) {
      GimpleCompoundRef ref = (GimpleCompoundRef) expr;
      return ref.getMember();
    }
    throw new UnsupportedOperationException(expr.toString());
  }

  public Map<String, GimpleType> getMembers() {
    return members;
  }
}
