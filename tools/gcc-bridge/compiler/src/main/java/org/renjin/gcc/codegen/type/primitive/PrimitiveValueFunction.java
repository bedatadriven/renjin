package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import java.util.Collections;
import java.util.List;


public class PrimitiveValueFunction implements ValueFunction {

  private Type type;
  private int byteSize;

  public PrimitiveValueFunction(GimplePrimitiveType type) {
    this.type = type.jvmType();
    this.byteSize = type.sizeOf();
  }
  
  public PrimitiveValueFunction(Type type) {
    this(GimplePrimitiveType.fromJvmType(type));
  }
  

  @Override
  public Type getValueType() {
    return type;
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return byteSize;
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    FatPtrExpr address = new FatPtrExpr(array, offset);
    JExpr value = Expressions.elementAt(array, offset);

    return new PrimitiveValue(value, address);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    PrimitiveValue primitiveValue = (PrimitiveValue) expr;
    return Collections.singletonList(primitiveValue.getExpr());
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.absent();
  }

  @Override
  public String toString() {
    return "Primitive[" + type + "]";
  }
}
