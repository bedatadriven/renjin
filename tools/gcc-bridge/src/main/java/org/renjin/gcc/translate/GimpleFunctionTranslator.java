package org.renjin.gcc.translate;


import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.FunctionPointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.jimple.*;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Translates a GimpleFunction to a Jimple function
 */
public class GimpleFunctionTranslator extends GimpleVisitor {

  private TranslationContext context;
  private JimpleMethodBuilder builder;
  private GimpleFunction function;

  private int nextTempId = 0;

  public GimpleFunctionTranslator(TranslationContext context) {
    this.context = context;
  }

  public void translate(GimpleFunction function) {
    this.function = function;
    this.builder = context.getMainClass().newMethod();
    builder.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
    builder.setName(function.getName());
    builder.setReturnType(Jimple.type(function.returnType()));

    for(GimpleParameter param : function.getParameters()) {
      context.resolveType(param.getType()).declareParameter(builder, param);
    }

    for(GimpleVarDecl varDecl : function.getVariableDeclarations()) {
      context.resolveType(varDecl.getType()).declareVariable(builder, varDecl);
    }

    function.visitIns(this);
  }

  @Override
  public void blockStart(GimpleBasicBlock bb) {
    builder.addLabel("label" + bb.getNumber());
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {
    builder.addStatement(Jimple.id(assignment.getRHS()) + " = " +
            translateExpr(assignment.getOperator(), assignment.getOperands()));
  }

  @Override
  public void visitReturn(GimpleReturn gimpleReturn) {
    if(gimpleReturn.getValue() == GimpleNull.INSTANCE) {
      builder.addStatement("return");
    } else {
      builder.addStatement("return " + translateExpr(gimpleReturn.getValue()));
    }
  }

  @Override
  public void visitGoto(Goto gotoIns) {
    builder.addStatement(new JimpleGoto(label(gotoIns.getTarget())));
  }

  private String label(GimpleLabel target) {
    return "label" + target.getBasicBlockNumber();
  }

  @Override
  public void visitCall(GimpleCall call) {

    StringBuilder stmt = new StringBuilder();
    if(call.getLhs() != null) {
      stmt.append(Jimple.id(call.getLhs()));
      stmt.append(" = ");
    }

    JimpleMethodRef method;
    if(call.getFunction() instanceof GimpleExternal) {
      method = context.resolveMethod(call);
      stmt.append("staticinvoke").append(method.signature());
    } else if(call.getFunction() instanceof GimpleVar) {
      GimpleType type = function.getType(call.getFunction());
      if(!(type instanceof FunctionPointerType)) {
        throw new UnsupportedOperationException("Function value must be of type FunctionPointer, got: " + type);
      }
      method = context.getFunctionPointerMethod((FunctionPointerType) type);
      stmt.append("interfaceinvoke ");
      stmt.append(translateExpr(call.getFunction()));
      stmt.append(".");
      stmt.append(method.signature());
    } else {
      throw new UnsupportedOperationException(call.getFunction().toString());
    }

    stmt.append("(");
    for(int i=0;i!=call.getParams().size();++i) {
      JimpleType type = method.getParameterTypes().get(i);
      GimpleExpr param = call.getParams().get(i);
      if(i > 0) {
        stmt.append(", ");
      }
      stmt.append(marshallParam(type, param));
    }
    stmt.append(")");
    builder.addStatement(stmt.toString());
  }

  private String marshallParam(JimpleType type, GimpleExpr param) {
    if(type.toString().startsWith("org.renjin.gcc.runtime.FunPtr")) {
      return marshalFunctionPointer(param);
    } else {
      return translateExpr(param);
    }
  }

  private String marshalFunctionPointer(GimpleExpr param) {
    if(param instanceof GimpleExternal) {
      JimpleMethodRef method = context.resolveMethod(((GimpleExternal) param).getName());
      String invokerClass = context.getInvokerClass(method);
      String ptr = declareTemp(new JimpleType(invokerClass));
      builder.addStatement(ptr + " = new " + invokerClass);
      builder.addStatement("specialinvoke " + ptr + ".<" + invokerClass + ": void <init>()>()");
      return ptr;
    } else if(param instanceof GimpleVar) {
      return translateExpr(param);
    } else {
      throw new UnsupportedOperationException(param.toString());
    }
  }

  private String declareTemp(JimpleType type) {
    String name = "_tmp" + (nextTempId++);
    builder.addVarDecl(type, name);
    return name;
  }

  @Override
  public void visitConditional(GimpleConditional conditional) {
    builder.addStatement("if " + translateExpr(conditional.getOperator(), conditional.getOperands()) +
            " goto " + label(conditional.getTrueTarget()));
    builder.addStatement(new JimpleGoto(label(conditional.getFalseTarget())));
  }

  private String translateExpr(GimpleOp operator, List<GimpleExpr> operands) {
    switch (operator) {
      case MULT_EXPR:
        return binaryInfix("*", operands);
      case PLUS_EXPR:
        return binaryInfix("+", operands);
      case MINUS_EXPR:
        return binaryInfix("-", operands);
      case NE_EXPR:
        return binaryInfix("!=", operands);
      case EQ_EXPR:
        return binaryInfix("==", operands);
      case RDIV_EXPR:
        return binaryInfix("/", operands);
      case LE_EXPR:
        return binaryInfix("<=", operands);
      case LT_EXPR:
        return binaryInfix("<", operands);
      case GT_EXPR:
        return binaryInfix(">", operands);
      case GE_EXPR:
        return binaryInfix(">=", operands);
      case TRUTH_NOT_EXPR:
        return unaryPrefix("!" , operands);
      case REAL_CST:
        return doubleConstant(constantValue(operands));
      case INTEGER_CST:
        return intConstant(constantValue(operands));
      case ADDR_EXPR:
        return addressOf(operands);
      case ABS_EXPR:
        return absValue(operands);
      case POINTER_PLUS_EXPR:
        return pointerPlus(operands);
      case INDIRECT_REF:
        return indirectRef(operands);
      case FLOAT_EXPR:
      case VAR_DECL:
      case NOP_EXPR:
      case SSA_NAME:
        return translateExpr(operands.get(0));
    }
    throw new UnsupportedOperationException(operator.name() + operands.toString());
  }

  private String addressOf(List<GimpleExpr> operands) {
    if(operands.get(0) instanceof GimpleExternal) {
      return marshalFunctionPointer(operands.get(0));
    }  else {
      throw new UnsupportedOperationException(operands.toString());
    }

  }

  private String absValue(List<GimpleExpr> operands) {
    return "staticinvoke <java.lang.Math: double abs(double)>(" + translateExpr(operands.get(0)) + ")";
  }


  private String binaryInfix(String operatorToken, List<GimpleExpr> operands) {
    return translateExpr(operands.get(0)) + " " + operatorToken + " " + translateExpr(operands.get(1));
  }

  private String unaryPrefix(String operator, List<GimpleExpr> operands) {
    return operator + translateExpr(operands.get(0));
  }

  private Object constantValue(List<GimpleExpr> operands) {
    if(operands.get(0) instanceof GimpleConstant) {
      return ((GimpleConstant) operands.get(0)).getValue();
    } else {
      throw new UnsupportedOperationException("Expected constant: " + operands.get(0));
    }
  }

  private String doubleConstant(Object value) {
     return Double.toString( ((Number)value).doubleValue() );
  }

  private String intConstant(Object value) {
    if(value instanceof Boolean) {
      return value == Boolean.TRUE ? "1" : "0";
    } else {
      return Integer.toString( ((Number)value).intValue() );
    }
  }

  private String translateExpr(GimpleExpr expr) {
    if(expr instanceof GimpleVar) {
      return Jimple.id((GimpleVar) expr);
    } else if(expr instanceof GimpleConstant) {
      return Jimple.constant(((GimpleConstant) expr).getValue());
    } else if(expr instanceof GimpleExternal) {
      return resolveExternal((GimpleExternal) expr);
    } else {
      throw new UnsupportedOperationException(expr.toString());
    }
  }

  private String resolveExternal(GimpleExternal external) {
    Field field = context.findField(external);
    return "<" + field.getDeclaringClass().getName() + ": " + Jimple.type(field.getType()) + " " + external.getName() + ">";
  }

  private String pointerPlus(List<GimpleExpr> operands) {
    GimpleVar pointer = (GimpleVar) operands.get(0);
    GimpleExpr increment = operands.get(1);
    return "virtualinvoke " + Jimple.id(pointer) +
            ".<org.renjin.gcc.runtime.Pointer: org.renjin.gcc.runtime.Pointer plus(int)>(" +
            translateExpr(increment) + ")";
  }

  private String indirectRef(List<GimpleExpr> operands) {
      GimpleVar pointer = (GimpleVar) operands.get(0);
    return "virtualinvoke " + Jimple.id(pointer) + ".<org.renjin.gcc.runtime.Pointer: double asDouble()>()";
  }
}
