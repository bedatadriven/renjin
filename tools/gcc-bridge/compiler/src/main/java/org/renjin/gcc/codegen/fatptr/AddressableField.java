package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;

import java.util.List;


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
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayField, arrayType.getDescriptor(), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, offsetField, "I", null, null);
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    
    // Reference value types like records and fat pointers may need
    // to initialize a value
    List<Value> initialValues = valueFunction.getDefaultValue();
    
    // Allocate a unit array store the value
    // (for value types like complex, this might actually be several elements)
    Value unitArray = Values.newArray(valueFunction.getValueType(), valueFunction.getElementLength(), initialValues);

    // Store this to the array field
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    unitArray.load(mv);
    mv.putfield(recordType, arrayField, arrayType);
  }

  @Override
  public ExprGenerator memberExprGenerator(Value instance) {
    Value array = Values.field(instance, arrayType, arrayField);
    Value offset = Values.field(instance, Type.INT_TYPE, offsetField);
    
    return valueFunction.dereference(array, offset);
  }
}
