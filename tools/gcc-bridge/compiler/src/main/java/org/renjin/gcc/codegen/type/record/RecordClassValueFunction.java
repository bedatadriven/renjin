package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ArrayElement;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.var.LocalVarAllocator;

import java.util.Collections;
import java.util.List;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;


/**
 * Translates a pointer array and offset to a Record value represented by a JVM Class.
 */
public class RecordClassValueFunction implements ValueFunction {
  
  private RecordClassTypeStrategy strategy;

  public RecordClassValueFunction(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public Type getValueType() {
    return strategy.getJvmType();
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return strategy.getRecordTypeDef().getSize() / 8;
  }

  @Override
  public RecordValue dereference(JExpr array, JExpr offset) {
    ArrayElement element = Expressions.elementAt(array, offset);
    FatPtrExpr address = new FatPtrExpr(array, offset);
    
    return new RecordValue(element, address);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    return Collections.singletonList(((RecordValue) expr).getRef());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, 
                         JExpr valueCount) {
    
    // If we have a small, fixed number of records to copy,
    // unroll the loop
    if(valueCount instanceof ConstantValue) {
      int length = ((ConstantValue) valueCount).getIntValue();
      if(length < 3) {
        for(int i=0;i<length;++i) {
          copyElement(mv, destinationArray, destinationOffset, sourceArray, sourceOffset, constantInt(i));
        }
        return;
      }
    }
    
    // Otherwise,
    // Loop over each element and invoke the set() method
    LocalVarAllocator.LocalVar counter = mv.getLocalVarAllocator().reserve(Type.INT_TYPE);
    counter.store(mv, constantInt(0));

    Label loopHead = new Label();
    Label loopBody = new Label();

    // Initialize our loop counter
    mv.goTo(loopHead);

    // Loop body
    mv.visitLabel(loopBody);

    // Copy record
    copyElement(mv, destinationArray, destinationOffset, sourceArray, sourceOffset, counter);
    
    mv.iinc(counter.getIndex(), 1);

    // Loop head
    mv.visitLabel(loopHead);
    counter.load(mv);
    valueCount.load(mv);
    mv.ificmplt(loopBody);

  }

  private void copyElement(MethodGenerator mv, 
                           JExpr destinationArray, JExpr destinationOffset, 
                           JExpr sourceArray, JExpr sourceOffset, 
                           JExpr index) {
    
    ArrayElement destRef = Expressions.elementAt(destinationArray, Expressions.sum(destinationOffset, index));
    ArrayElement sourceRef = Expressions.elementAt(sourceArray, Expressions.sum(sourceOffset, index));

    destRef.load(mv);
    sourceRef.load(mv);
    mv.invokevirtual(getValueType(), "set", Type.getMethodDescriptor(Type.VOID_TYPE, getValueType()), false);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.<JExpr>of(new RecordConstructor(strategy));
  }

  @Override
  public String toString() {
    return "RecordClass[" + strategy.getRecordTypeDef().getName() + "]";
  }

}
