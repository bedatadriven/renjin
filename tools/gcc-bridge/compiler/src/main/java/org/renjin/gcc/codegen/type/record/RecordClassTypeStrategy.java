package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;

import java.util.Map;

/**
 * Strategy for variables and values of type {@code GimpleRecordType} that employs JVM classes
 */
public class RecordClassTypeStrategy extends RecordTypeStrategy<RecordValue> implements SimpleTypeStrategy<RecordValue> {

  private TypeOracle typeOracle;
  private boolean unitPointer;
  private RecordLayout layout;

  public RecordClassTypeStrategy(TypeOracle typeOracle, GimpleRecordTypeDef recordTypeDef, RecordLayout layout) {
    super(recordTypeDef);
    this.typeOracle = typeOracle;
    this.layout = layout;
  }

  public Type getJvmType() {
    return layout.getType();
  }

  @Override
  public RecordValue wrap(JExpr expr) {
    return new RecordValue(expr);
  }
  

  public boolean isUnitPointer() {
    return unitPointer;
  }

  public void setUnitPointer(boolean unitPointer) {
    this.unitPointer = unitPointer;
  }

  @Override
  public final ParamStrategy getParamStrategy() {
    return new RecordClassParamStrategy(this);
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new SimpleReturnStrategy(this);
  }

  @Override
  public RecordValue variable(GimpleVarDecl decl, VarAllocator allocator) {

    JLValue instance = allocator.reserve(decl.getName(), layout.getType(), new RecordConstructor(this));

    if(isUnitPointer()) {
      // If we are using the RecordUnitPtr strategy, then the record value is also it's address
      return new RecordValue(instance, new RecordUnitPtr(instance));

    } else if(decl.isAddressable()) {
      JLValue unitArray = allocator.reserveUnitArray(decl.getName(), layout.getType(), Optional.of((JExpr)instance));
      FatPtrPair address = new FatPtrPair(new RecordClassValueFunction(this), unitArray);
      JExpr value = Expressions.elementAt(address.getArray(), 0);
      return new RecordValue(value, address);

    } else {
      
      return new RecordValue(instance);
    }
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new RecordFieldStrategy(this, className, fieldName);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    if(isUnitPointer()) {
      // If this type is a unit pointer, we don't need to do anything special
      return new RecordFieldStrategy(this, className, fieldName);
    } else {
      return new AddressableField(getJvmType(), fieldName, new RecordClassValueFunction(this));
    }
  }
  
  @Override
  public RecordValue constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    Map<GimpleFieldRef, GExpr> fields = Maps.newHashMap();
    for (GimpleConstructor.Element element : value.getElements()) {
      GExpr fieldValue = exprFactory.findGenerator(element.getValue());
      fields.put((GimpleFieldRef) element.getField(), fieldValue);
    }
    return new RecordValue(new RecordConstructor(typeOracle, this, fields));
  }
  

  @Override
  public GExpr memberOf(RecordValue instance, GimpleFieldRef fieldRef, TypeStrategy fieldTypeStrategy) {
    return layout.memberOf(instance, fieldRef);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, new RecordClassValueFunction(this));
  }

  @Override
  public RecordValue cast(GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof RecordClassTypeStrategy) {
      return (RecordValue) value;
    
    } 
    throw new UnsupportedCastException();
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    if(unitPointer) {
      return new RecordUnitPtrStrategy(this);
    } else {
      return new FatPtrStrategy(new RecordClassValueFunction(this));
    }
  }

  public RecordUnitPtrStrategy pointerToUnit() {
    return new RecordUnitPtrStrategy(this);
  }

  @Override
  public String toString() {
    return "RecordClassTypeStrategy[" + recordTypeDef.getName() + "]";
  }
}
