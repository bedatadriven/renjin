/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.codegen.expr;


import org.renjin.compiler.codegen.EmitContext;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public abstract class SexpExpr implements CompiledSexp {

  private final Type declaredType;

  public SexpExpr(Type declaredType) {
    this.declaredType = declaredType;
  }

  public SexpExpr() {
    this(Type.getType(SEXP.class));
  }

  @Override
  public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {

    loadSexp(context, mv);

    // Cast to AtomicVector
    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(AtomicVector.class));

    switch (vectorType) {
      case LOGICAL:
      case INT:
        mv.invokeinterface(Type.getInternalName(AtomicVector.class), "toIntArray", "()[I");
        break;
      case DOUBLE:
        mv.invokeinterface(Type.getInternalName(AtomicVector.class), "toDoubleArray", "()[D");
        break;
      default:
        throw new UnsupportedOperationException("TODO: " + vectorType);
    }
  }

  @Override
  public void loadLength(EmitContext context, InstructionAdapter mv) {
    loadSexp(context, mv);
    mv.invokeinterface(Type.getInternalName(SEXP.class), "length", "()I");
  }


  @Override
  public final void loadScalar(EmitContext context, InstructionAdapter mv, VectorType vectorType) {

    // Load the SEXP onto the stack
    loadSexp(context, mv);

    // Cast to Vector
    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Vector.class));

    // Request first element
    mv.visitInsn(Opcodes.ICONST_0);

    switch (vectorType) {
      case BYTE:
        mv.invokeinterface(Type.getInternalName(Vector.class), "getElementAsByte", "(I)B");
        break;
      case LOGICAL:
      case INT:
        mv.invokeinterface(Type.getInternalName(Vector.class), "getElementAsInt", "(I)I");
        break;
      case DOUBLE:
        mv.invokeinterface(Type.getInternalName(Vector.class), "getElementAsDouble", "(I)D");
        break;
      case STRING:
        mv.invokeinterface(Type.getInternalName(Vector.class), "getElementAsString", "(I)Ljava/lang/String;");
        break;
      default:
        throw new IllegalArgumentException(vectorType.toString());
    }
  }

  @Override
  public CompiledSexp elementAt(EmitContext context, CompiledSexp indexExpr) {
    return new SexpElementAt(this, indexExpr);
  }
}
