package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitivePtrPlus;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.CharTypes;

import java.util.List;

/**
 * Implements __ctype_b_loc.
 * 
 * @see CharTypes
 * 
 */
public class CharTypeBLocCall implements CallGenerator {
  
  public static final String NAME = "__ctype_b_loc";
  
  @Override
  public void emitCall(MethodVisitor visitor, List<ExprGenerator> argumentGenerators) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitCallAndPopResult(MethodVisitor visitor, List<ExprGenerator> argumentGenerators) {
    // NOOP
  }

  @Override
  public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
    return new ShortPtrPtr();
  }
  
  private class ShortPtrPtr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(new GimplePointerType(new GimpleIntegerType(16)));
    }

    @Override
    public ExprGenerator valueOf() {
      return new ShortPtr();
    }
  }
  
  private class ShortPtr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(new GimpleIntegerType(16));
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(CharTypes.class), "TABLE", "[S");
      mv.visitLdcInsn(CharTypes.OFFSET);
    }

    @Override
    public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
      return new PrimitivePtrPlus(this, offsetInBytes);
    }
  }
}
