package org.renjin.gcc.codegen.type.record;

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
 * Represents a record with a primitive array.
 * 
 * <p>For records that have only </p>
 */
public class RecordArrayTypeStrategy extends RecordTypeStrategy<SimpleExpr> {
  
  public RecordArrayTypeStrategy(GimpleRecordTypeDef recordTypeDef) {
    super(recordTypeDef);
  }

  @Override
  public void linkFields(TypeOracle typeOracle) {
    
  }

  @Override
  public void writeClassFiles(File outputDirectory) throws IOException {

  }

  @Override
  public Expr memberOf(SimpleExpr instance, GimpleFieldRef fieldRef) {
    return null;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return null;
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return null;
  }

  @Override
  public SimpleExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    return null;
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    return null;
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return null;
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return null;
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return null;
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return null;
  }
}
