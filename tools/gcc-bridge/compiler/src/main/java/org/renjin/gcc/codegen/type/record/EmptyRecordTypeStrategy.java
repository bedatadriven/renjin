package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;

/**
 * Strategy for empty record types. Since such a record type has zero length and no fields.
 */
public class EmptyRecordTypeStrategy extends RecordTypeStrategy<SimpleExpr> {
  public EmptyRecordTypeStrategy(GimpleRecordTypeDef recordTypeDef) {
    super(recordTypeDef);
    Preconditions.checkState(recordTypeDef.getFields().size() == 0);
  }
  
  public static boolean accept(GimpleRecordTypeDef recordTypeDef) {
    return recordTypeDef.getFields().isEmpty();
  }
  
  @Override
  public void linkFields(TypeOracle typeOracle) {
    // NOOP
  }

  @Override
  public void writeClassFiles(File outputDirectory) throws IOException {
    // NOOP
  }

  @Override
  public Expr memberOf(SimpleExpr instance, GimpleFieldRef fieldRef) {
    throw new UnsupportedOperationException("No fields");
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new EmptyRecordParamStrategy();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new EmptyRecordReturnStrategy();
  }

  @Override
  public SimpleExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    return new EmptyRecordVar();
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new EmptyRecordField();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new EmptyRecordPtrTypeStrategy();
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("TODO");
  }

}
