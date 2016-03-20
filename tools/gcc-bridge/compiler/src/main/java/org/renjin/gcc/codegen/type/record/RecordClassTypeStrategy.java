package org.renjin.gcc.codegen.type.record;

import com.google.common.collect.Maps;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Strategy for variables and values of type {@code GimpleRecordType} that employs JVM classes
 */
public class RecordClassTypeStrategy extends RecordTypeStrategy<SimpleExpr> {

  private Type jvmType;
  private boolean provided;
  private boolean unitPointer;

  private Map<String, FieldStrategy> fields = null;

  public RecordClassTypeStrategy(GimpleRecordTypeDef recordTypeDef) {
    super(recordTypeDef);
  }

  public Type getJvmType() {
    if(jvmType == null) {
      throw new IllegalStateException("Type name of record " + getRecordType().getName() + " has not been initialized.");
    }
    return jvmType;
  }

  public void setJvmType(Type jvmType) {
    this.jvmType = jvmType;
  }

  /**
   *
   * @return true if the class backing this record type is already provided by an existing JVM class
   */
  public boolean isProvided() {
    return provided;
  }

  public void setProvided(boolean provided) {
    this.provided = provided;
  }

  public boolean isUnitPointer() {
    return unitPointer;
  }

  public void setUnitPointer(boolean unitPointer) {
    this.unitPointer = unitPointer;
  }

  @Override
  public void linkFields(TypeOracle typeOracle) {
    fields = new HashMap<>();
    for (GimpleField gimpleField : getRecordTypeDef().getFields()) {
      FieldStrategy fieldStrategy = typeOracle.forField(getJvmType(), gimpleField);
      fields.put(gimpleField.getName(), fieldStrategy);
    }
  }

  @Override
  public final ParamStrategy getParamStrategy() {
    return new SimpleParamStrategy(jvmType);
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    SimpleLValue instance = allocator.reserve(decl.getName(), jvmType, new RecordConstructor(this));
    if(isUnitPointer()) {
      // If we are using the RecordUnitPtr strategy, then the record value is also it's address
      return new SimpleAddressableExpr(instance, instance);
    } else {
      return instance;
    }
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new RecordFieldStrategy(this, fieldName);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(getJvmType(), fieldName, new RecordValueFunction(this));
  }

  public Expr voidCast(Expr voidPtr) {
    //return new VoidCastExprGenerator(voidPtr, getRecordType(), jvmType);
    throw new UnsupportedOperationException();
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    Map<GimpleFieldRef, Expr> fields = Maps.newHashMap();
    for (GimpleConstructor.Element element : value.getElements()) {
      Expr fieldValue = exprFactory.findGenerator(element.getValue());
      fields.put((GimpleFieldRef) element.getField(), fieldValue);
    }
    return new RecordConstructor(this, fields);
  }


  @Override
  public void writeClassFiles(File outputDirectory) throws IOException {
    if(isProvided()) {
      return;
    }

    RecordClassGenerator classGenerator = new RecordClassGenerator(jvmType, fields.values());
    classGenerator.writeClassFile(outputDirectory);
  }


  @Override
  public Expr memberOf(SimpleExpr instance, GimpleFieldRef fieldRef) {
    if(fields == null) {
      throw new IllegalStateException("Fields map is not yet initialized.");
    }
    FieldStrategy fieldStrategy = fields.get(fieldRef.getName());
    if(fieldStrategy == null) {
      throw new InternalCompilerException(
          String.format("No field named '%s' in record type '%s'", fieldRef.getName(), jvmType));
    }
    return fieldStrategy.memberExprGenerator(instance);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, new RecordValueFunction(this));
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    if(unitPointer) {
      return new RecordUnitPtrStrategy(this);
    } else {
      return new FatPtrStrategy(new RecordValueFunction(this));
    }
  }

  public RecordUnitPtrStrategy pointerToUnit() {
    return new RecordUnitPtrStrategy(this);
  }
}
