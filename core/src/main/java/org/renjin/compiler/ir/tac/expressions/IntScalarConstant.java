package org.renjin.compiler.ir.tac.expressions;


import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.TypeBounds;

import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class IntScalarConstant extends Constant {

  private final int value;

  public IntScalarConstant(int value) {
    this.value = value;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    switch(value) {
      case 0:
        mv.visitInsn(ICONST_0);
        break;
      case 1:
        mv.visitInsn(ICONST_1);
        break;
      case 2:
        mv.visitInsn(ICONST_2);
        break;
      case 3:
        mv.visitInsn(ICONST_3);
        break;
      case 4:
        mv.visitInsn(ICONST_4);
        break;
      case 5:
        mv.visitInsn(ICONST_5);
        break;
      default:
        if(value < 127) {
          mv.visitIntInsn(BIPUSH, value);
        } else {
          throw new UnsupportedOperationException("todo: " + value);
        }
    }
    return 1;
  }

  @Override
  public TypeBounds computeTypeBounds(Map<LValue, TypeBounds> variableMap) {
    return TypeBounds.scalarInt();
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
