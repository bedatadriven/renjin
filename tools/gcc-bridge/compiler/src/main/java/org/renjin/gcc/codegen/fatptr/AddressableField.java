package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;


public class AddressableField extends FieldStrategy {

  private final Type recordType;
  private final String arrayField;
  private final String offsetField;
  private final Type arrayType;
  private ValueFunction valueFunction;
  

  public AddressableField(Type recordType, String fieldName, ValueFunction valueFunction) {
    this.recordType = recordType;
    this.arrayField = fieldName;
    this.arrayType = Type.getType("[" + valueFunction.getValueType().getDescriptor());
    this.offsetField = fieldName + "$offset";
    this.valueFunction = valueFunction;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayField, arrayType.getDescriptor(), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, offsetField, "I", null, null);
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    
    // Reference value types like records and fat pointers may need
    // to initialize a value
    Optional<JExpr> initialValues = valueFunction.getValueConstructor();
    
    // Allocate a unit array store the value
    // (for value types like complex, this might actually be several elements)
    JExpr unitArray = Expressions.newArray(valueFunction.getValueType(), valueFunction.getElementLength(), initialValues);

    // Store this to the array field
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    unitArray.load(mv);
    mv.putfield(recordType, arrayField, arrayType);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int fieldOffset, TypeStrategy expectedType) {
    JExpr array = Expressions.field(instance, arrayType, arrayField);
    JExpr offset = Expressions.field(instance, Type.INT_TYPE, offsetField);
    
    return valueFunction.dereference(array, offset);
  }

}
