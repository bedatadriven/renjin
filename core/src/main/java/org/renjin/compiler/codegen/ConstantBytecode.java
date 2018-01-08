/**
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
package org.renjin.compiler.codegen;

import org.apache.commons.math.util.DoubleArray;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

/**
 * Routines to generate bytecode for constant values
 */
public class ConstantBytecode {


  public static void generateAttributes(InstructionAdapter mv, AttributeMap constantAttributes) {

    // SEXP should be on the stack
    // Create new AttributeMap.Builder

    Type builderType = pushAttributeBuilder(mv, constantAttributes);
    // Stack:
    // SEXP AttrbuteMap.Builder
    mv.invokeinterface(Type.getInternalName(SEXP.class), "setAttributes",
        Type.getMethodDescriptor(Type.getType(SEXP.class), builderType));

  }

  /**
   * Generates the bytecode to push an AttributeBuilder instance onto the stack.
   */
  public static Type pushAttributeBuilder(InstructionAdapter mv, AttributeMap constantAttributes) {
    Type builderType = Type.getType(AttributeMap.Builder.class);
    mv.invokestatic(Type.getInternalName(AttributeMap.class), "newBuilder",
        Type.getMethodDescriptor(builderType), false);

    // Now Builder is on the stack...
    for (PairList.Node node : constantAttributes.nodes()) {
      if(node.getTag() == Symbols.CLASS) {
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "setClass",
            Type.getMethodDescriptor(builderType, Type.getType(SEXP.class)), false);

      } else if(node.getTag() == Symbols.NAMES) {
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "setNames",
            Type.getMethodDescriptor(builderType, Type.getType(SEXP.class)), false);

      } else if(node.getTag() == Symbols.DIM) {
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "setDim",
            Type.getMethodDescriptor(builderType, Type.getType(SEXP.class)), false);

      } else if(node.getTag() == Symbols.DIMNAMES) {
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "setDimNames",
            Type.getMethodDescriptor(builderType, Type.getType(SEXP.class)), false);

      } else {
        mv.aconst(node.getTag().getPrintName());
        pushConstant(mv, node.getValue());
        mv.invokevirtual(builderType.getInternalName(), "set",
            Type.getMethodDescriptor(builderType, Type.getType(String.class), Type.getType(SEXP.class)), false);
      }
    }
    return builderType;
  }

  public static void pushAttributes(InstructionAdapter mv, AttributeMap constantAttributes) {
    pushAttributeBuilder(mv, constantAttributes);
    mv.invokevirtual(Type.getInternalName(AttributeMap.Builder.class), "build",
        Type.getMethodDescriptor(Type.getType(AttributeMap.class)), false);
  }

  public static void pushConstant(InstructionAdapter mv, SEXP value) {
    if(value == Null.INSTANCE) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Null.class), "INSTANCE", Type.getDescriptor(Null.class));
      return;
    }
    if(value instanceof StringVector) {
      if(value.length() == 1 && value.getAttributes().isEmpty()) {
        mv.visitLdcInsn(((StringVector) value).getElementAsString(0));
        mv.invokestatic(Type.getInternalName(StringVector.class), "valueOf",
            Type.getMethodDescriptor(Type.getType(StringVector.class), Type.getType(String.class)), false);
        return;
      }
    } else if(value instanceof DoubleVector) {
      if(value.length() == 1) {
        mv.anew(Type.getType(DoubleArrayVector.class));
        mv.dup();
        mv.dconst(((DoubleVector) value).getElementAsDouble(0));
        pushAttributes(mv, value.getAttributes());

        mv.invokespecial(Type.getInternalName(DoubleArrayVector.class), "<init>",
            Type.getMethodDescriptor(Type.VOID_TYPE, Type.DOUBLE_TYPE, Type.getType(AttributeMap.class)), false);

        return;
      }
    }

    throw new UnsupportedOperationException("TODO: constant = " + value);
  }
}
