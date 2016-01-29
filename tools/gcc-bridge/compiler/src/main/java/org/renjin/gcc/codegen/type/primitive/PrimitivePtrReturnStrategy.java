package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.SWAP;

/**
 * Strategy for returning pointer to primitive values using wrapped fat pointers such
 * as {@link org.renjin.gcc.runtime.IntPtr} or {@link org.renjin.gcc.runtime.DoublePtr}.
 */
public class PrimitivePtrReturnStrategy implements ReturnStrategy {
  
  private GimpleIndirectType type;
  private WrapperType wrapperType;

  public PrimitivePtrReturnStrategy(GimpleType type) {
    this.type = (GimpleIndirectType) type;
    this.wrapperType = WrapperType.of(this.type.getBaseType());
  }

  @Override
  public Type getType() {
    return wrapperType.getWrapperType();
  }

  @Override
  public void emitReturnValue(MethodGenerator mv, ExprGenerator valueGenerator) {
    wrapperType.emitPushNewWrapper(mv, valueGenerator);

    // return
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitReturnDefault(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    return new CallExpr(callGenerator, arguments);
  }
  
  private class CallExpr extends AbstractExprGenerator {
    private CallGenerator callGenerator;
    private List<ExprGenerator> arguments;

    public CallExpr(CallGenerator callGenerator, List<ExprGenerator> arguments) {
      this.callGenerator = callGenerator;
      this.arguments = arguments;
    }

    @Override
    public GimpleType getGimpleType() {
      return type;
    }

    @Override
    public void emitPushPointerWrapper(MethodGenerator mv) {

      // emit the call, which will push the wrapper pointer value on the stack
      callGenerator.emitCall(mv, arguments);
    
    }
    
    @Override
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
   
      emitPushPointerWrapper(mv);

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

    @Override
    public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
      return new PrimitivePtrPlus(this, offsetInBytes);
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedPrimitiveValue(this);
    }
  }
}
