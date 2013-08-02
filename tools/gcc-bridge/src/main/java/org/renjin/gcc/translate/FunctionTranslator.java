package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.ins.GimpleAssign;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.gimple.ins.GimpleConditional;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.ins.GimpleReturn;
import org.renjin.gcc.gimple.ins.GimpleSwitch;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.ins.GimpleGoto;
import org.renjin.gcc.gimple.type.GimpleVoidType;
import org.renjin.gcc.jimple.*;
import org.renjin.gcc.translate.call.CallTranslator;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.marshall.Marshallers;
import org.renjin.gcc.translate.type.ImPrimitiveType;

/**
 * Translates a GimpleFunction to a Jimple function
 */
public class FunctionTranslator extends GimpleVisitor {

  private TranslationContext translationContext;
  private JimpleMethodBuilder builder;

  private FunctionContext context;

  public FunctionTranslator(TranslationContext translationContext) {
    this.translationContext = translationContext;
  }

  public void translate(GimpleFunction function) {
    try {
      this.builder = translationContext.getMainClass().newMethod();
      builder.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
      builder.setName(function.getMangledName());
      if(function.getReturnType() instanceof GimpleVoidType) {
        builder.setReturnType(JimpleType.VOID);
      } else {
        builder.setReturnType(translationContext.resolveType(function.getReturnType()).returnType());
      }

      context = new FunctionContext(translationContext, function, builder);

      function.visitIns(this);
      builder.finish();

    } catch (Exception e) {
      throw new TranslationException("Exception translating function " + function.getName(), e);
    }
  }

  @Override
  public void blockStart(GimpleBasicBlock bb) {
    builder.addLabel(basicBlockLabel(bb.getIndex()));
  }

  private String basicBlockLabel(int index) {
    return "BB" + index;
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {
    try {
      AssignmentTranslator translator = new AssignmentTranslator(context);
      translator.translate(assignment);
    } catch(Exception e) {
      throw new RuntimeException("Exception translating " + assignment, e);
    }
  }

  @Override
  public void visitReturn(GimpleReturn gimpleReturn) {
    
    if(gimpleReturn.getValue() == null) {
      builder.addStatement("return");
    } else {
      ImExpr returnValue = context.resolveExpr(gimpleReturn.getValue());

      builder.addStatement("return " + Marshallers.marshallReturnValue(context, returnValue));
    }
  }

  @Override
  public void visitGoto(GimpleGoto gotoIns) {
    builder.addStatement(new JimpleGoto(basicBlockLabel(gotoIns.getTarget())));
  }

  @Override
  public void visitSwitch(GimpleSwitch gimpleSwitch) {
    ImExpr switchExpr = context.resolveExpr(gimpleSwitch.getValue());

    JimpleSwitchStatement jimpleSwitch = new JimpleSwitchStatement(
        switchExpr.translateToPrimitive(context, ImPrimitiveType.INT));

    for (GimpleSwitch.Case branch : gimpleSwitch.getCases()) {
      jimpleSwitch.addBranch(branch.getLow(), basicBlockLabel(branch.getBasicBlockIndex()));
    }

    if(gimpleSwitch.getDefaultCase() != null) {
      jimpleSwitch.addDefaultBranch(basicBlockLabel(gimpleSwitch.getDefaultCase().getBasicBlockIndex()));
    }

    builder.add(jimpleSwitch);
  }

  @Override
  public void visitCall(GimpleCall call) {
    try {
      CallTranslator translator = context.getTranslationContext().getCallTranslator(call);
      translator.writeCall(context, call);

    } catch(Exception e) {
      throw new TranslationException("Exception thrown while translating call " + call, e);
    }
  }

  @Override
  public void visitConditional(GimpleConditional conditional) {
    try {
      new ConditionalTranslator(context).translate(conditional);
    } catch (Exception e) {
      throw new RuntimeException("Exception translating " + conditional, e);
    }
  }
}
