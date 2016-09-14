package org.renjin.compiler.pipeline.accessor;

import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.VectorPipeliner;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Vector;

import static org.renjin.repackaged.asm.Opcodes.*;

public class VirtualAccessor extends Accessor {

  private static final Logger LOGGER = Logger.getLogger(VirtualAccessor.class.getName());
  
  /**
   * The local variable where we're storing the
   * pointer to the Vector object
   */
  private int ptrLocalIndex;
  private String vectorClass;
  private int operandIndex;
  private Vector.Type vectorType;

  public VirtualAccessor(Vector vector, int operandIndex) {
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
}
