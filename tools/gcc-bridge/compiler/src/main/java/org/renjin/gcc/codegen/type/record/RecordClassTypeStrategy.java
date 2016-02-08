package org.renjin.gcc.codegen.type.record;

import com.google.common.collect.Maps;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.AddressableValue;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Var;
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
public class RecordClassTypeStrategy extends RecordTypeStrategy<Value> {

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
      FieldStrategy fieldStrategy = typeOracle.forField(getJvmType().getInternalName(), gimpleField);
      fields.put(gimpleField.getName(), fieldStrategy);
    }
  }

  @Override
  public final ParamStrategy getParamStrategy() {
    return new ValueParamStrategy(jvmType);
  }

  @Override
  public Value varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    Var instance = allocator.reserve(decl.getName(), jvmType, new RecordConstructor(this));
    if(isUnitPointer()) {
      // If we are using the RecordUnitPtr strategy, then the record value is also it's address
      return new AddressableValue(instance, instance);
    } else {
      return instance;
    }
  }

  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    return new RecordFieldStrategy(className, fieldName, this);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
    return new AddressableField(getJvmType(), fieldName, new RecordValueFunction(this));
  }

  public ExprGenerator voidCast(ExprGenerator voidPtr) {
    //return new VoidCastExprGenerator(voidPtr, getRecordType(), jvmType);
    throw new UnsupportedOperationException();
  }

  @Override
  public Value constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    Map<GimpleFieldRef, ExprGenerator> fields = Maps.newHashMap();
    for (GimpleConstructor.Element element : value.getElements()) {
      ExprGenerator fieldValue = exprFactory.findGenerator(element.getValue());
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
  public ExprGenerator memberOf(Value instance, GimpleFieldRef fieldRef) {
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
  public TypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, new RecordValueFunction(this));
  }

  @Override
  public TypeStrategy pointerTo() {
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
