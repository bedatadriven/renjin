package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.guava.base.Preconditions;

import javax.annotation.Nonnull;

import static org.renjin.gcc.codegen.expr.Expressions.*;

/**
 * Exposes a byte array as an int value
 */
public class ByteArrayAsInt implements SimpleLValue {
  
  private SimpleExpr array;
  private SimpleExpr offset;

  public ByteArrayAsInt(SimpleExpr array, SimpleExpr offset) {
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
    
    SimpleExpr[] bytes = new SimpleExpr[4];
    SimpleExpr b1 = elementAt(array, sum(offset, 0));
    SimpleExpr b2 = elementAt(array, sum(offset, 1));
    SimpleExpr b3 = elementAt(array, sum(offset, 2));
    SimpleExpr b4 = elementAt(array, sum(offset, 3));

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
  public void store(MethodGenerator mv, SimpleExpr rhs) {
    Preconditions.checkArgument(rhs.getType().equals(Type.INT_TYPE));

    SimpleExpr[] bytes = new SimpleExpr[] {
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
