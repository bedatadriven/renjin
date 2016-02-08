package org.renjin.gcc.codegen.array;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

import java.util.List;

/**
 * Type strategy for arrays of values
 */
public class ArrayTypeStrategy extends TypeStrategy<FatPtrExpr> {

  private int lowerBound;
  private final ValueFunction valueFunction;
  private boolean parameterWrapped = true;

  public ArrayTypeStrategy(GimpleArrayType arrayType, ValueFunction valueFunction) {
    this.lowerBound = arrayType.getLbound();
    this.valueFunction = valueFunction;
  }

  public boolean isParameterWrapped() {
    return parameterWrapped;
  }

  public ArrayTypeStrategy setParameterWrapped(boolean parameterWrapped) {
    this.parameterWrapped = parameterWrapped;
    return this;
  }

  @Override
  public TypeStrategy pointerTo() {
    return new FatPtrStrategy(new ArrayValueFunction(valueFunction))
        .setParametersWrapped(parameterWrapped);
  }

  @Override
  public FatPtrExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor constructor) {
    List<Value> values = Lists.newArrayList();
    for (GimpleConstructor.Element element : constructor.getElements()) {
     values.add(exprFactory.findValueGenerator(element.getValue())); 
    }
    
    Value array = Values.newArray(valueFunction.getValueType(), values);
    Value offset = Values.zero();
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public FatPtrExpr varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    int arrayLength = ((GimpleArrayType) decl.getType()).getElementCount() * valueFunction.getElementLength();

    Value initialValue = Values.newArray(valueFunction.getValueType(), arrayLength);
    Value array = allocator.reserve(decl.getName(), arrayType, initialValue);
    Value offset = Values.zero();
    
    return new FatPtrExpr(new FatPtrExpr(array, offset), array, offset);
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator array, ExprGenerator index) {
    FatPtrExpr arrayFatPtr = (FatPtrExpr) array;
    
    Value indexValue = (Value) index;
    
    // New offset  = ptr.offset + (index * value.length)
    // for arrays of doubles, for example, this will be the same as ptr.offset + index
    // but for arrays of complex numbers, this will be ptr.offset + (index * 2)
    Value newOffset = Values.sum(
        arrayFatPtr.getOffset(),
        Values.product(
            Values.difference(indexValue, lowerBound), 
            valueFunction.getElementLength()));
    
    return valueFunction.dereference(arrayFatPtr.getArray(), newOffset);
  }

}
