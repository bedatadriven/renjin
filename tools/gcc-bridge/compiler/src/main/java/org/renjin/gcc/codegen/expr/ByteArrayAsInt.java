package org.renjin.gcc.codegen.expr;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;

import javax.annotation.Nonnull;

import static org.renjin.gcc.codegen.expr.Expressions.*;

/**
 * Exposes a byte array as an int value
 */
public class ByteArrayAsInt implements JLValue {
  
  private JExpr array;
  private JExpr offset;

  public ByteArrayAsInt(JExpr array, JExpr offset) {
    this.array = array;
    this.offset = offset;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.INT_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {

    // b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);

    JExpr b1 = elementAt(array, sum(offset, 0));
    JExpr b2 = elementAt(array, sum(offset, 1));
    JExpr b3 = elementAt(array, sum(offset, 2));
    JExpr b4 = elementAt(array, sum(offset, 3));

    // (b1 & 0xFF);
    b1.load(mv);
    mv.cast(Type.BYTE_TYPE, Type.INT_TYPE);
    mv.iconst(0xFF);
    mv.and(Type.INT_TYPE);
    
    // (b2 & 0xFF) << 8
    b2.load(mv);
    mv.cast(Type.BYTE_TYPE, Type.INT_TYPE);
    mv.iconst(0xFF);
    mv.and(Type.INT_TYPE);
    mv.iconst(8);
    mv.shl(Type.INT_TYPE);
    
    mv.or(Type.INT_TYPE);

    // (b2 & 0xFF) << 16
    b3.load(mv);
    mv.cast(Type.BYTE_TYPE, Type.INT_TYPE);
    mv.iconst(0xFF);
    mv.and(Type.INT_TYPE);
    mv.iconst(16);
    mv.shl(Type.INT_TYPE);

    mv.or(Type.INT_TYPE);

    // b1 << 24 
    b4.load(mv);
    mv.iconst(24);
    mv.shl(Type.INT_TYPE);

    mv.or(Type.INT_TYPE);
    
  }


  @Override
  public void store(MethodGenerator mv, JExpr rhs) {
    Preconditions.checkArgument(rhs.getType().equals(Type.INT_TYPE));

    JExpr[] bytes = new JExpr[] {
        castPrimitive(shiftRight(rhs, 0), Type.BYTE_TYPE),
        castPrimitive(shiftRight(rhs, 8), Type.BYTE_TYPE),
        castPrimitive(shiftRight(rhs, 16), Type.BYTE_TYPE),
        castPrimitive(shiftRight(rhs, 24), Type.BYTE_TYPE) };
    
    for (int i=0;i<bytes.length;++i) {
      ArrayElement element = Expressions.elementAt(array, sum(offset, i));
      element.store(mv, bytes[i]);
    }
  }
}
