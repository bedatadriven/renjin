package org.renjin.gcc.translate;


import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.FunctionPointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.jimple.*;

import java.lang.reflect.Field;

/**
 * Translates a GimpleFunction to a Jimple function
 */
public class GimpleFunctionTranslator extends GimpleVisitor {

  private TranslationContext translationContext;
  private JimpleMethodBuilder builder;
  private GimpleFunction function;

  private FunctionContext context;


  public GimpleFunctionTranslator(TranslationContext translationContext) {
    this.translationContext = translationContext;
  }

  public void translate(GimpleFunction function) {
    try {
      this.function = function;
      this.builder = translationContext.getMainClass().newMethod();
      builder.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
      builder.setName(function.getName());
      builder.setReturnType(Jimple.type(function.returnType()));

      context = new FunctionContext(translationContext, function, builder);

      function.visitIns(this);
    } catch(Exception e) {
      throw new TranslationException("Exception translating function " + function.getName(), e);
    }
  }

  @Override
  public void blockStart(GimpleBasicBlock bb) {
    builder.addLabel(bb.getName());
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {
    try {
      context.lookupVar(assignment.getRHS()).assign(assignment.getOperator(), assignment.getOperands());
    } catch(Exception e) {
      throw new TranslationException("Exception translating " + assignment, e);
    }
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
    builder.addStatement(new JimpleGoto(gotoIns.getTarget().getName()));
  }

  @Override
  public void visitLabelIns(GimpleLabelIns labelIns) {
    builder.addLabel(labelIns.getLabel().getName());
  }

  @Override
  public void visitSwitch(GimpleSwitch gimpleSwitch) {
    JimpleSwitchStatement jimpleSwitch = new JimpleSwitchStatement(translateExpr(gimpleSwitch.getExpr()));
    for(GimpleSwitch.Branch branch : gimpleSwitch.getBranches()) {
      jimpleSwitch.addBranch(branch.getValue(), branch.getLabel().getName());
    }
    builder.add(jimpleSwitch);
  }


  @Override
  public void visitCall(GimpleCall call) {
    try {

      StringBuilder stmt = new StringBuilder();
      if(call.getLhs() != null) {
        stmt.append(Jimple.id(call.getLhs()));
        stmt.append(" = ");
      }

      JimpleMethodRef method;
      if(call.getFunction() instanceof GimpleExternal) {
        method = translationContext.resolveMethod(call);
        stmt.append("staticinvoke").append(method.signature());
      } else if(call.getFunction() instanceof GimpleVar) {
        GimpleType type = function.getType(call.getFunction());
        if(!(type instanceof FunctionPointerType)) {
          throw new UnsupportedOperationException("Function value must be of type FunctionPointer, got: " + type);
        }
        method = translationContext.getFunctionPointerMethod((FunctionPointerType) type);
        stmt.append("interfaceinvoke ");
        stmt.append(translateExpr(call.getFunction()));
        stmt.append(".");
        stmt.append(method.signature());
      } else {
        throw new UnsupportedOperationException(call.getFunction().toString());
      }

      // check arity
      if(method.getParameterTypes().size() != call.getParamCount()) {
        throw new TranslationException("Argument count of " + method + " does not match call");
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
    } catch(Exception e) {
      throw new TranslationException("Exception thrown while translating call " + call, e);
    }
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
      JimpleMethodRef method = translationContext.resolveMethod(((GimpleExternal) param).getName());
      String invokerClass = translationContext.getInvokerClass(method);
      String ptr = context.declareTemp(new JimpleType(invokerClass));
      builder.addStatement(ptr + " = new " + invokerClass);
      builder.addStatement("specialinvoke " + ptr + ".<" + invokerClass + ": void <init>()>()");
      return ptr;
    } else if(param instanceof GimpleVar) {
      return translateExpr(param);
    } else {
      throw new UnsupportedOperationException(param.toString());
    }
  }

  @Override
  public void visitConditional(GimpleConditional conditional) {
    try {
      new ConditionalTranslator(context).translate(conditional);
    } catch(Exception e) {
      throw new RuntimeException("Exception translating " + conditional, e);
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
    Field field = translationContext.findField(external);
    return "<" + field.getDeclaringClass().getName() + ": " + Jimple.type(field.getType()) + " " + external.getName() + ">";
  }
}
