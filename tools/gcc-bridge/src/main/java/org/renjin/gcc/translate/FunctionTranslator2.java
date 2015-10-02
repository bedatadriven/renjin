package org.renjin.gcc.translate;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.ins.*;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.translate.expr.ImExpr;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Translates a GimpleFunction to a Jimple function
 */
public class FunctionTranslator2 extends GimpleVisitor {

  private TranslationContext translationContext;

  private FunctionContext context;
  private MethodNode methodNode;

  public FunctionTranslator2(TranslationContext translationContext) {
    this.translationContext = translationContext;
  }

  public void translate(GimpleFunction function) {
    try {

      JimpleMethodBuilder dummyToRemove = translationContext.getMainClass().newMethod();
      
      context = new FunctionContext(translationContext, function, dummyToRemove);

      methodNode = new MethodNode();
      methodNode.access = ACC_PUBLIC | ACC_STATIC;
      methodNode.name = function.getMangledName();
      methodNode.desc = descriptor(function);
      

      function.visitIns(this);
      
      translationContext.getMainClass().addMethod(methodNode);

    } catch (Exception e) {
      throw new TranslationException("Exception compiling function " + function.getName(), e);
    }
  }

  /**
   * Composes the JVM method signature for this function. For example, "(D)D" or "()V"
   */
  private String descriptor(GimpleFunction function) {
    Type returnType = context.resolveType(function.getReturnType()).jvmReturnType();
    Type[] paramTypes = new Type[function.getParameters().size()];
    for (int i = 0; i < function.getParameters().size(); i++) {
      paramTypes[i] = context.resolveType(function.getParameters().get(i).getType()).jvmParamType();
    }
    
    return Type.getMethodDescriptor(returnType, paramTypes);
  }

  @Override
  public void blockStart(GimpleBasicBlock bb) {
    methodNode.instructions.add(new LabelNode(context.label(bb.getIndex())));
  }
  
  @Override
  public void visitAssignment(GimpleAssign assignment) {
    
    try {
      AssignmentTranslator2 translator = new AssignmentTranslator2(context);
      translator.translate(assignment);
    } catch(Exception e) {
      throw new RuntimeException("Exception translating " + assignment, e);
    }
  }

  @Override
  public void visitReturn(GimpleReturn gimpleReturn) {
    ImExpr value = context.resolveExpr(gimpleReturn.getValue());
    value.writePush(context);
    
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitGoto(GimpleGoto gotoIns) {
    methodNode.instructions.add(new JumpInsnNode(Opcodes.GOTO, new LabelNode(context.label(gotoIns.getTarget()))));
  }

  @Override
  public void visitSwitch(GimpleSwitch gimpleSwitch) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitCall(GimpleCall call) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitConditional(GimpleConditional conditional) {
    throw new UnsupportedOperationException();
  }
}
