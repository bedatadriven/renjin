package org.renjin.compiler.pipeline.accessor;

import com.google.common.base.Optional;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.sexp.IntVector;

import static org.objectweb.asm.Opcodes.*;

public abstract class Accessor {

  public abstract void init(ComputeMethod method);

  /**
   * Writes the bytecode instructions to push the element of this vector at {@code index} onto the stack as a 
   * {@code double}.
   *
   */
  public final void pushElementAsDouble(ComputeMethod method) {
    pushElementAsDouble(method, Optional.<Label>absent());
  }
  
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
  }

  /**
   * Writes the bytecode instructions to push the length of this vector onto the stack.
   * The operand index MUST be the next element on the stack.
   */
  public abstract void pushLength(ComputeMethod method);


  /**
   * Writes the bytecode instructions to push the element of this vector at {@code index} onto the stack as an 
   * {@code int}.
   *
   */
  protected final void pushIntConstant(MethodVisitor mv, int value) {
    if(value == 0) {
      mv.visitInsn(ICONST_0);
    } else if(value == 1) {
      mv.visitInsn(ICONST_1);
    } else if(value == 2) {
      mv.visitInsn(ICONST_2);
    } else if(value == 3) {
      mv.visitInsn(ICONST_3);
    } else if(value == 4) {
      mv.visitInsn(ICONST_4);
    } else if(value == 5) {
      mv.visitInsn(ICONST_5);
    } else if(value < Byte.MAX_VALUE) {
      mv.visitIntInsn(BIPUSH, value);
    } else {
      throw new UnsupportedOperationException("operandIndex: " + value);
    }
  }

  /**
   * Writes the bytecode instructions to push the element of this vector at {@code index} onto the stack as an 
   * {@code int}.
   *
   */
  public final void pushElementAsInt(ComputeMethod method, int index) {
    MethodVisitor mv = method.getVisitor();
    pushIntConstant(mv, index);
    pushElementAsInt(method, Optional.<Label>absent());
  }

  /**
   * Writes the bytecode instructions to push an element of this vector onto the stack as an 
   * {@code int}. The operand index MUST be the next element on the stack.
   * 
   * If an {@code naLabel} is provided, then an NA check will be performed, jumping to the provided
   * {@code naLabel} if the value is NA.
   */
  public void pushElementAsInt(ComputeMethod method, Optional<Label> naLabel) {
    pushElementAsDouble(method);
    method.getVisitor().visitInsn(D2I);
  }

  /**
   * Writes the bytecode instructions to perform an integer NA check on the next value on the stack 
   * if an {@code naLabel} is provided.
   */
  protected final void doIntegerNaCheck(MethodVisitor mv, Optional<Label> naLabel) {
    if(naLabel.isPresent()) {
      // stack => { ... , value }
      mv.visitInsn(DUP);
      // stack => { ... , value, value }
      mv.visitFieldInsn(GETSTATIC, "org/renjin/sexp/IntVector", "NA", "I");
      // stack => { ... , value, value, NA }
      mv.visitJumpInsn(IF_ICMPEQ, naLabel.get());
      // stack => { ... , value }
    }
  }


  /**
   * Reports whether it is necessary to check for integer NAs when accessing or computing elements.
   */
  public abstract boolean mustCheckForIntegerNAs();
  
}
