/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.pipeliner.fusion.node;

import org.renjin.pipeliner.ComputeMethod;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public abstract class LoopNode {

  public abstract void init(ComputeMethod method);

  /**
   * Writes the bytecode instructions to push the element of this vector at {@code index} onto the stack as a 
   * {@code double}.
   *
   */
  public final void pushElementAsDouble(ComputeMethod method) {
    pushElementAsDouble(method, Optional.<Label>absent());
  }
  
  public abstract void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel);

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
      mv.visitLdcInsn(value);
    }
  }
  
  protected final void cast(MethodVisitor mv, Type fromType, Type toType) {
    if(fromType.equals(toType)) {
      return;
    }
    if(fromType.getSort() == Type.INT) {
      if(toType.getSort() == Type.DOUBLE) {
        mv.visitInsn(Opcodes.I2D);
        return;
      }
    } 
    if(fromType.getSort() == Type.DOUBLE) {
      if(toType.getSort() == Type.INT) {
        mv.visitInsn(Opcodes.D2I);
        return;
      }
    }
    throw new UnsupportedOperationException("Cast from " + fromType + " to " + toType);
  }


  protected final void cast(MethodVisitor mv, Class<?> fromType, Class<?> toType) {
    if(fromType.equals(toType)) {
      return;
    }
    if(fromType.equals(int.class)) {
      if(toType.equals(double.class)) {
        mv.visitInsn(Opcodes.I2D);
        return;
      }
    }
    if(fromType.equals(double.class)) {
      if(toType.equals(int.class)) {
        mv.visitInsn(Opcodes.D2I);
        return;
      }
    }
    throw new UnsupportedOperationException("Cast from " + fromType + " to " + toType);
  }


  protected static boolean supportedType(Class<?> type) {
    return type.equals(double.class) ||
        type.equals(int.class);
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

  /**
   * Adds this node's properties to a cache loopup key for the LoopKernel computation.
   *
   * <p>Implementations must write first a unique identifier for this LoopNode's subclass, and then any additional
   * properties that affect the generated bytecode.</p>
   */
  public abstract void appendToKey(StringBuilder key);

}
