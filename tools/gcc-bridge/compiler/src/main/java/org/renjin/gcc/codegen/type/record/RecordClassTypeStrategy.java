package org.renjin.gcc.codegen.type.record;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.fat.RecordArrayFieldStrategy;
import org.renjin.gcc.codegen.type.record.fat.RecordFatPtrStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidCastExprGenerator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy for variables and values of type {@code GimpleRecordType} that employs JVM classes
 */
public class RecordClassTypeStrategy extends RecordTypeStrategy {

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
    return new RecordParamStrategy(this);
  }

  @Override
  public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    return new RecordVarGenerator(this, allocator.reserve(decl.getName(), jvmType));
  }

  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    return new RecordFieldStrategy(className, fieldName, this);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
    return new AddressableRecordField(className, fieldName, this);
  }

  public ExprGenerator voidCast(ExprGenerator voidPtr) {
    return new VoidCastExprGenerator(voidPtr, getRecordType(), jvmType);
  }

  @Override
  public ExprGenerator constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    Map<String, ExprGenerator> fields = Maps.newHashMap();
    for (GimpleConstructor.Element element : value.getElements()) {
      ExprGenerator fieldValue = exprFactory.findGenerator(element.getValue());
      fields.put(element.getFieldName(), fieldValue);
    }
    return new RecordConstructor(this, fields);
  }


  public FieldStrategy getFieldGenerator(String name) {
    if(fields == null) {
      throw new IllegalStateException("Fields map is not yet initialized.");
    }
    FieldStrategy fieldStrategy = fields.get(name);
    if(fieldStrategy == null) {
      throw new InternalCompilerException(String.format("No field named '%s' in record type '%s'", name, jvmType));
    }
    return fieldStrategy;
  }

  public void emitConstructor(MethodGenerator mv) {
    mv.visitTypeInsn(Opcodes.NEW, getJvmType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, getJvmType().getInternalName(), "<init>", "()V", false);
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
  public TypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new Array(arrayType);
  }


  public class Array extends TypeStrategy {

    private GimpleArrayType arrayType;

    public Array(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public FieldStrategy fieldGenerator(String className, String fieldName) {
      return new RecordArrayFieldStrategy(className, fieldName, RecordClassTypeStrategy.this, arrayType);
    }

    @Override
    public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
      return fieldGenerator(className, fieldName);
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return new RecordArrayVarGenerator(arrayType, RecordClassTypeStrategy.this,
          allocator.reserveArrayRef(decl.getName(), getJvmType()));
    }

    @Override
    public ExprGenerator constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {

      if(arrayType.getElementCount() != value.getElements().size()) {
        throw new InternalCompilerException(String.format(
            "array type defined as size of %d, only %d constructors provided",
            arrayType.getElementCount(), value.getElements().size()));
      }

      List<ExprGenerator> elements = Lists.newArrayList();
      for (GimpleConstructor.Element element : value.getElements()) {
        GimpleConstructor elementValue = (GimpleConstructor) element.getValue();
        ExprGenerator elementConstructor = exprFactory.findGenerator(elementValue);
        elements.add(elementConstructor);
      }

      return new RecordArrayConstructor(RecordClassTypeStrategy.this, arrayType, elements);
    }
  }

  @Override
  public TypeStrategy pointerTo() {
    if(unitPointer) {
      return new RecordUnitPtrStrategy(this);
    } else {
      return new RecordFatPtrStrategy(this);
    }
  }
  
  public RecordUnitPtrStrategy pointerToUnit() {
    return new RecordUnitPtrStrategy(this);
  }
}
