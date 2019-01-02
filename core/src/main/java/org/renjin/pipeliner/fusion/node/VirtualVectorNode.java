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
package org.renjin.pipeliner.fusion.node;

import org.renjin.pipeliner.ComputeMethod;
import org.renjin.pipeliner.VectorPipeliner;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Vector;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.logging.Logger;

import static org.renjin.repackaged.asm.Opcodes.*;

public class VirtualVectorNode extends LoopNode {

  private static final Logger LOGGER = Logger.getLogger(VirtualVectorNode.class.getName());
  
  /**
   * The local variable where we're storing the
   * pointer to the Vector object
   */
  private int ptrLocalIndex;
  private String vectorClass;
  private int operandIndex;
  private Vector.Type vectorType;

  public VirtualVectorNode(int operandIndex, Vector vector) {
    if(VectorPipeliner.DEBUG) {
      System.out.println("VirtualAccessor for " + vector.getClass().getName());
    }
    this.vectorType = vector.getVectorType();
    
    // we really want to reference this class as specifically as possible
    if(!Modifier.isPublic(vector.getClass().getModifiers())) {
      LOGGER.warning("Vector class " + vector.getClass().getName() + " is not public: member access may not be fully inlined by JVM.");
    } 
    this.vectorClass = findFirstPublicSuperClass(vector.getClass()).getName().replace('.', '/');
    this.operandIndex = operandIndex;
  }

  
  private Class findFirstPublicSuperClass(Class clazz) {
    while(!Modifier.isPublic(clazz.getModifiers())) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }


  public void init(ComputeMethod method) {

    ptrLocalIndex = method.reserveLocal(1);

    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, method.getOperandsLocalIndex());
    pushIntConstant(mv, operandIndex);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, vectorClass);
    mv.visitVarInsn(ASTORE, ptrLocalIndex);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, ptrLocalIndex);
    mv.visitMethodInsn(INVOKEVIRTUAL, vectorClass, "length", "()I");
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, ptrLocalIndex);
    mv.visitInsn(SWAP);
    mv.visitMethodInsn(INVOKEVIRTUAL, vectorClass, "getElementAsDouble", "(I)D");
  }

  @Override
  public void pushElementAsInt(ComputeMethod method, Optional<Label> naLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, ptrLocalIndex);
    mv.visitInsn(SWAP);
    mv.visitMethodInsn(INVOKEVIRTUAL, vectorClass, "getElementAsInt", "(I)I");
    
    doIntegerNaCheck(mv, naLabel);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return vectorType == IntVector.VECTOR_TYPE;
  }

  @Override
  public void appendToKey(StringBuilder key) {
    key.append(vectorClass);
  }
}
