package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;

import java.util.List;


/**
 * Strategy for returning a pointer to pointers of primitive values, such as {@code double**},
 * using an {@link ObjectPtr}.
 * 
 * <p>Each element of the {@link ObjectPtr#array} will be an instance of a primitive pointer
 * wrapper such as {@link org.renjin.gcc.runtime.IntPtr} or {@link org.renjin.gcc.runtime.DoublePtr}.</p>
 */
public class PrimitivePtrPtrReturnStrategy implements ReturnStrategy {
  
  private GimpleType pointerPointerType;

  public PrimitivePtrPtrReturnStrategy(GimpleType pointerPointerType) {
    this.pointerPointerType = pointerPointerType;
  }

  @Override
  public Type getType() {
    return Type.getType(ObjectPtr.class);
  }

  @Override
  public void emitReturnValue(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushPointerWrapper(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitReturnDefault(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    return new CallExpr(callGenerator, arguments);
  }
  
  private class CallExpr extends AbstractExprGenerator {

    private final CallGenerator callGenerator;
    private final List<ExprGenerator> arguments;

    public CallExpr(CallGenerator callGenerator, List<ExprGenerator> arguments) {
      this.callGenerator = callGenerator;
      this.arguments = arguments;
    }

    @Override
    public GimpleType getGimpleType() {
      return pointerPointerType;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      // Invoke the method, which will push the ObjectPtr wrapper onto the stack
      callGenerator.emitCall(mv, arguments);
    
      // Now unpack the array and offset fields and place them onto the stack,
      // casting the array to the right primitive 
      GimpleIndirectType baseType = pointerPointerType.getBaseType();
      WrapperType baseWrapperType = WrapperType.forPointerType(baseType);

      WrapperType.OBJECT_PTR.emitUnpackArrayAndOffset(mv, baseWrapperType);
    }
  }
}
