package org.renjin.gcc.translate;


import java.lang.reflect.Field;

import org.renjin.gcc.gimple.GimpleAssign;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCall;
import org.renjin.gcc.gimple.GimpleConditional;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleLabelIns;
import org.renjin.gcc.gimple.GimpleReturn;
import org.renjin.gcc.gimple.GimpleSwitch;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.Goto;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleCompoundRef;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleExternal;
import org.renjin.gcc.gimple.expr.GimpleIndirection;
import org.renjin.gcc.gimple.expr.GimpleNull;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleGoto;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleModifiers;
import org.renjin.gcc.jimple.JimpleSwitchStatement;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.call.CallTranslator;
import org.renjin.gcc.translate.types.PrimitiveTypes;
import org.renjin.gcc.translate.var.Variable;

/**
 * Translates a GimpleFunction to a Jimple function
 */
public class FunctionTranslator extends GimpleVisitor {

  private TranslationContext translationContext;
  private JimpleMethodBuilder builder;
  private GimpleFunction function;

  private FunctionContext context;


  public FunctionTranslator(TranslationContext translationContext) {
    this.translationContext = translationContext;
  }

  public void translate(GimpleFunction function) {
    try {
      this.function = function;
      this.builder = translationContext.getMainClass().newMethod();
      builder.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
      builder.setName(function.getName());  
      builder.setReturnType(translateReturnType(function.returnType()));

      context = new FunctionContext(translationContext, function, builder);

      function.visitIns(this);
    } catch(Exception e) {
      throw new TranslationException("Exception translating function " + function.getName(), e);
    }
  }

  private JimpleType translateReturnType(GimpleType returnType) {
    if(returnType instanceof PrimitiveType) {
      return PrimitiveTypes.get((PrimitiveType) returnType);
    } else if(returnType instanceof PointerType) {
      GimpleType innerType = ((PointerType) returnType).getInnerType();
      if(innerType instanceof PrimitiveType) {
        return PrimitiveTypes.getWrapperType((PrimitiveType) innerType);
      }
    }
    throw new UnsupportedOperationException(returnType.toString());
  }

  @Override
  public void blockStart(GimpleBasicBlock bb) {
    builder.addLabel(bb.getName());
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {
    try {
      if(assignment.getLHS() instanceof GimpleVar) {
        context.lookupVar((GimpleVar) assignment.getLHS()).assign(assignment.getOperator(), assignment.getOperands());
      
      } else if(assignment.getLHS() instanceof GimpleCompoundRef) {
        GimpleCompoundRef ref = (GimpleCompoundRef) assignment.getLHS();
        Variable var = context.lookupVar(ref.getVar());
        var.assignMember(ref.getMember(), assignment.getOperator(), assignment.getOperands());
      
      } else if(assignment.getLHS() instanceof GimpleIndirection) {
        GimpleIndirection indirectRef = (GimpleIndirection) assignment.getLHS();
        Variable var = context.lookupVar(indirectRef.getPointer());
        var.assignIndirect(assignment.getOperator(), assignment.getOperands());
        
      } else {
        throw new UnsupportedOperationException("lhs: " + assignment.getLHS());
      }
    } catch(Exception e) {
      throw new TranslationException("Exception translating " + assignment, e);
    }
  }

  @Override
  public void visitReturn(GimpleReturn gimpleReturn) {
    if(gimpleReturn.getValue() == GimpleNull.INSTANCE) {
      builder.addStatement("return");
    } else {
      if(gimpleReturn.getValue() instanceof GimpleVar) {
        Variable var = context.lookupVar(gimpleReturn.getValue());
        builder.addStatement("return " + var.returnExpr());
      } else {
        builder.addStatement("return " + translateExpr(gimpleReturn.getValue()));
      }
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
    new CallTranslator(context, call).translate();
//    try {
//
//      JimpleExpr callExpr = translateCallExpr(call);
//
//      if(call.getLhs() instanceof GimpleVar) {
//        Variable lhs = context.lookupVar(call.getLhs());
//        assignCallResult(lhs, callExpr);
//        
//      } else if(call.getLhs() == null) {
//        builder.addStatement(callExpr.toString());
//
//      } else {
//        throw new UnsupportedOperationException("Lvalue: " + call.getLhs());
//      }
//      
//      
//    } catch(Exception e) {
//      throw new TranslationException("Exception thrown while translating call " + call, e);
//    }
  }
//
//  private JimpleExpr translateCallExpr(GimpleCall call) {
//    StringBuilder callExpr = new StringBuilder();
//    MethodRef method;
//    if(call.getFunction() instanceof GimpleExternal) {
//      method = translationContext.resolveMethod(call, function.getCallingConvention());
//      callExpr.append("staticinvoke").append(method.signature());
//      
//    } else if(call.getFunction() instanceof GimpleVar) {
//      GimpleType type = function.getType(call.getFunction());
//      if(!(type instanceof FunctionPointerType)) {
//        throw new UnsupportedOperationException("Function value must be of type FunctionPointer, got: " + type);
//      }
//      method = translationContext.getFunctionPointerMethod((FunctionPointerType) type);
//      callExpr.append("interfaceinvoke ");
//      callExpr.append(translateExpr(call.getFunction()));
//      callExpr.append(".");
//      callExpr.append(method.signature());
//    } else {
//      throw new UnsupportedOperationException(call.getFunction().toString());
//    }
//
//    // check arity
//    if(method.getParameterTypes().size() != call.getParamCount()) {
//      throw new TranslationException("Argument count of " + method + " does not match call");
//    }
//
//    callExpr.append("(");
//    for(int i=0;i!=call.getParams().size();++i) {
//      JimpleType type = method.getParameterTypes().get(i);
//      GimpleExpr param = call.getParams().get(i);
//      if(i > 0) {
//        callExpr.append(", ");
//      }
//      callExpr.append(marshallParam(type, param));
//    }
//    callExpr.append(")");
//    return new JimpleExpr(callExpr.toString());
//  }

//  private String marshallParam(JimpleType type, GimpleExpr param) {
//    if(type.toString().startsWith("org.renjin.gcc.runtime.FunPtr")) {
//      return marshalFunctionPointer(param);
//    } else {
//      return translateExpr(param);
//    }
//  }
//
//  private String marshalFunctionPointer(GimpleExpr param) {
//    if(param instanceof GimpleExternal) {
//      MethodRef method = translationContext.resolveMethod(((GimpleExternal) param).getName());
//      String invokerClass = translationContext.getInvokerClass(method);
//      String ptr = context.declareTemp(new RealJimpleType(invokerClass));
//      builder.addStatement(ptr + " = new " + invokerClass);
//      builder.addStatement("specialinvoke " + ptr + ".<" + invokerClass + ": void <init>()>()");
//      return ptr;
//    } else if(param instanceof GimpleVar) {
//      return translateExpr(param);
//    } else {
//      throw new UnsupportedOperationException(param.toString());
//    }
//  }

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
      Variable var = context.lookupVar(expr);
      return var.returnExpr().toString();
    } else if(expr instanceof GimpleConstant) {
      return Jimple.constant(((GimpleConstant) expr).getValue());
    } else if(expr instanceof GimpleExternal) {
      return resolveExternal((GimpleExternal) expr);
    } else if(expr instanceof GimpleAddressOf) {
      return translateAddressOf((GimpleAddressOf) expr);
    } else {
      throw new UnsupportedOperationException(expr.toString());
    }
  }

  private String translateAddressOf(GimpleAddressOf expr) {
    Variable var = context.lookupVar(expr.getExpr());
    return var.wrapPointer().toString();
  }

  private String resolveExternal(GimpleExternal external) {
    Field field = translationContext.findField(external);
    return "<" + field.getDeclaringClass().getName() + ": " + Jimple.type(field.getType()) + " " + external.getName() + ">";
  }
}
