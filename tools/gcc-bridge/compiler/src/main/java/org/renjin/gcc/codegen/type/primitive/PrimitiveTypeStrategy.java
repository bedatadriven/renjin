package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.AddressableValue;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

/**
 * Creates {@code Generators} for {@code GimplePrimitiveType}.
 * 
 * <p>This is the easiest case, because there is a one-to-one correspondence between primitive
 * types in {@code Gimple} and on the JVM.</p>
 */
public class PrimitiveTypeStrategy extends TypeStrategy {
  
  private GimplePrimitiveType type;

  public PrimitiveTypeStrategy(GimplePrimitiveType type) {
    this.type = type;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new ValueParamStrategy(type.jvmType());
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new ValueReturnStrategy(type.jvmType());
  }

  @Override
  public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
    // TODO: return new AddressablePrimitiveField(className, fieldName, type, type.jvmType());
    throw new UnsupportedOperationException();
  }

  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    return new ValueFieldStrategy(type.jvmType(), fieldName);
  }

  @Override
  public ExprGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      Var unitArray = allocator.reserveUnitArray(decl.getName(), type.jvmType());
      FatPtrExpr address = new FatPtrExpr(unitArray);
      Value value = Values.elementAt(address.getArray(), 0);
      return new AddressableValue(value, address);
      
    } else {
      return allocator.reserve(decl.getName(), type.jvmType());
    }
  }

  @Override
  public TypeStrategy pointerTo() {
    return new FatPtrStrategy(new PrimitiveValueFunction());
  }

  @Override
  public TypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(new PrimitiveValueFunction());
  }

  private class PrimitiveValueFunction implements ValueFunction {

    @Override
    public Type getValueType() {
      return type.jvmType();
    }

    @Override
    public int getElementLength() {
      return 1;
    }

    @Override
    public int getElementSize() {
      return type.sizeOf();
    }

    @Override
    public ExprGenerator dereference(Value array, Value offset) {
      FatPtrExpr address = new FatPtrExpr(array, offset);
      Value value = Values.elementAt(array, offset);
      
      return new AddressableValue(value, address);
    }
  }
}
