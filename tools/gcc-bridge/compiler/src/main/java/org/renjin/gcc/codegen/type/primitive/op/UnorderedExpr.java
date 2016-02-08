package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;

import javax.annotation.Nonnull;

/**
 * Determines whether {@code x} and {@code y} are "unordered", that is, 
 * if one or both arguments are NaN.
 */
public class UnorderedExpr implements SimpleExpr {
  
  private SimpleExpr x;
  private SimpleExpr y;

  public UnorderedExpr(SimpleExpr x, SimpleExpr y) {
    this.x = x;
    this.y = y;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    Label unordered = new Label();
    Label exit = new Label();
    
    emitIsNaN(mv, x);
    // 1 = not a number
    // 0 = not (not a number)
    // If not equal to zero, then x is 
    mv.ifne(unordered);
    
    // If x was not NaN, then we have to check y too
    emitIsNaN(mv, y);
    mv.ifne(unordered);
    
    // Ordered!
    mv.iconst(0);
    mv.goTo(exit);
    
    // Unordered
    mv.mark(unordered);
    mv.iconst(1);
    
    // Exit
    mv.mark(exit);
  }

  private void emitIsNaN(MethodGenerator mv, SimpleExpr value) {
    value.load(mv);

    switch (value.getType().getSort()) {
      case Type.DOUBLE:
        mv.invokestatic(Double.class, "isNaN", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.DOUBLE_TYPE));
        break;
      
      case Type.FLOAT:
        mv.invokestatic(Double.class, "isNaN", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.FLOAT_TYPE));
        break;          
    
      default:
        throw new IllegalStateException("type: " + value.getType());
    }
  }
}
