package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.SWAP;


public class PtrCallExprGenerator implements PtrGenerator {
 
  private final CallGenerator callGenerator;
  private List<ExprGenerator> arguments;
  private final WrapperType wrapperType;

  public PtrCallExprGenerator(WrapperType wrapperType, CallGenerator callGenerator, List<ExprGenerator> arguments) {
    this.wrapperType = wrapperType;
    this.callGenerator = callGenerator;
    this.arguments = arguments;
  }

  @Override
  public GimpleType gimpleBaseType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type baseType() {
    return wrapperType.getBaseType();
  }

  @Override
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    // emit the call, which will push the wrapper pointer value on the stack
    callGenerator.emitCall(mv, arguments);
    
    mv.visitInsn(Opcodes.DUP);

    // stack: [ wrapper ptr, wrapper ptr ]

    // Consume the first reference to the wrapper type and push the array field on the stack
    mv.visitFieldInsn(GETFIELD, wrapperType.getWrapperType().getInternalName(), "array", wrapperType.getArrayType().getDescriptor());
    
    // stack: [ wrapper ptr, array] 
    
    mv.visitInsn(SWAP);
    
    // stack: [ array, wrapper ptr ]
    
    mv.visitFieldInsn(GETFIELD, wrapperType.getWrapperType().getInternalName(), "offset", "I");
    
    // stack: [ array, offset ]
  
  }
}
