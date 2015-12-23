package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates the bytecode to cast a primitive value to a new type
 */
public class CastGenerator extends AbstractExprGenerator implements ExprGenerator {
  
  private ExprGenerator valueGenerator;
  private GimplePrimitiveType destinationType;

  public CastGenerator(ExprGenerator valueGenerator, GimplePrimitiveType destinationType) {
    this.valueGenerator = valueGenerator;
    this.destinationType = destinationType;
  }
  

  @Override
  public GimpleType getGimpleType() {
    return destinationType;
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {

    valueGenerator.emitPrimitiveValue(mv);

    cast(mv, valueGenerator.getJvmPrimitiveType(), destinationType.jvmType());
  }

  public static void cast(MethodVisitor mv, Type from, Type to) {
    if(!from.equals(to)) {
      if (from.equals(Type.INT_TYPE)) {
        /*
         * CONVERT FROM INT
         */
        if (to.equals(Type.LONG_TYPE)) {
          mv.visitInsn(Opcodes.I2L);

        } else if (to.equals(Type.FLOAT_TYPE)) {
          mv.visitInsn(Opcodes.I2F);

        } else if (to.equals(Type.DOUBLE_TYPE)) {
          mv.visitInsn(Opcodes.I2D);

        } else {
          cast(mv, from, Type.INT_TYPE);
        }
      } else if (from.equals(Type.LONG_TYPE)) {
        /*
         * CONVERT FROM LONG 
         */
        if (to.equals(Type.INT_TYPE)) {
          mv.visitInsn(Opcodes.L2I);
        } else if (to.equals(Type.FLOAT_TYPE)) {
          mv.visitInsn(Opcodes.L2F);
        } else if (to.equals(Type.DOUBLE_TYPE)) {
          mv.visitInsn(Opcodes.L2D);
        } else {
          cast(mv, from, Type.INT_TYPE);
        }
      } else if (from.equals(Type.FLOAT_TYPE)) {
        /*
         * CONVERT FROM FLOAT
         */
        if (to.equals(Type.DOUBLE_TYPE)) {
          mv.visitInsn(Opcodes.F2D);
          
        } else if (to.equals(Type.LONG_TYPE)) {
          mv.visitInsn(Opcodes.F2L);
        
        } else if (to.equals(Type.INT_TYPE)) {
          mv.visitInsn(Opcodes.F2I);
        
        } else {
          cast(mv, from, Type.INT_TYPE);
        }
      } else if (from.equals(Type.DOUBLE_TYPE)) {
        /*
         * CONVERT FROM DOUBLE
         */
        if (to.equals(Type.FLOAT_TYPE)) {
          mv.visitInsn(Opcodes.D2F);
          
        } else if (to.equals(Type.LONG_TYPE)) {
          mv.visitInsn(Opcodes.D2L);
          
        } else if (to.equals(Type.INT_TYPE)) {
          mv.visitInsn(Opcodes.D2I);
        
        } else {
          cast(mv, from, Type.INT_TYPE);
        }
      } else {
        /* 
         * CONVERT FROM BOOL, SHORT, CHAR, ETC
         */
        cast(mv, Type.INT_TYPE, to);
      }
    }
  }
}
