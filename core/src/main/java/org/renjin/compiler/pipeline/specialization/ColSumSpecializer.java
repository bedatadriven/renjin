package org.renjin.compiler.pipeline.specialization;

import com.google.common.base.Optional;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.accessor.Accessor;
import org.renjin.compiler.pipeline.accessor.Accessors;
import org.renjin.compiler.pipeline.accessor.InputGraph;

import static org.objectweb.asm.Opcodes.*;


public class ColSumSpecializer implements FunctionSpecializer {

  @Override
  public void compute(ComputeMethod method, DeferredNode node) {

    InputGraph inputGraph = new InputGraph(node);
    MethodVisitor mv = method.getVisitor();

    Accessor matrix = Accessors.create(node.getOperand(0), inputGraph);
    matrix.init(method);

    Accessor numColumnsAccessor = Accessors.create(node.getOperand(1), inputGraph);
    numColumnsAccessor.init(method);

    int numColumns = method.reserveLocal(1);

    // create the array to hold the sums
    int resultArray = method.reserveLocal(1);
    numColumnsAccessor.pushElementAsInt(method, 0);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, numColumns);
    mv.visitIntInsn(NEWARRAY, T_DOUBLE);
    mv.visitVarInsn(ASTORE, resultArray);

    // numRows = length / numColumns
    int numRows = method.reserveLocal(1);
    matrix.pushLength(method);
    mv.visitVarInsn(ILOAD, numColumns);
    mv.visitInsn(IDIV);
    mv.visitVarInsn(ISTORE, numRows);

    // initialize our counters
    int colIndex = method.declareCounter();
    int rowIndex = method.declareCounter();
    int sourceIndex = method.declareCounter();

    int sum = method.reserveLocal(2);
    mv.visitInsn(DCONST_0);
    mv.visitVarInsn(DSTORE, sum);

    Label loopHead = new Label();
    Label nextElement = new Label();

    Optional<Label> integerNaLabel;
    if(matrix.mustCheckForIntegerNAs()) {
      integerNaLabel = Optional.of(new Label());
    } else {
      integerNaLabel = Optional.absent();
    }

    // START THE LOOP!!
    mv.visitLabel(loopHead);

    // load the next element onto the stack
    mv.visitVarInsn(ILOAD, sourceIndex);
    matrix.pushElementAsDouble(method, integerNaLabel);

    // load the current sum on the stack, add this value to it,
    // and then store back to the local variable slot
    mv.visitVarInsn(DLOAD, sum);
    mv.visitInsn(DADD);
    mv.visitVarInsn(DSTORE, sum);

    mv.visitJumpInsn(GOTO, nextElement);

    // HANDLE THE INTEGER NA CASE:
    if(integerNaLabel.isPresent()) {
      mv.visitLabel(integerNaLabel.get());
      // discard the NA value on the stack
      mv.visitInsn(POP);
    }

    // GO TO NEXT ELEMENT
    mv.visitLabel(nextElement);

    // increment sourceIndex and rowIndex
    mv.visitIincInsn(sourceIndex, 1);
    mv.visitIincInsn(rowIndex, 1);

    // if we haven't reached the end of the column (rowIndex == numRows),
    // add the next element to the sum
    mv.visitVarInsn(ILOAD, rowIndex);
    mv.visitVarInsn(ILOAD, numRows);
    mv.visitJumpInsn(IF_ICMPNE, loopHead);

    // otherwise, add to the next row
    // save the sum to the result array
    mv.visitVarInsn(ALOAD, resultArray);
    mv.visitVarInsn(ILOAD, colIndex);
    mv.visitVarInsn(DLOAD, sum);
    mv.visitInsn(DASTORE);

    // reset row index
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, rowIndex);

    // reset sum
    mv.visitInsn(DCONST_0);
    mv.visitVarInsn(DSTORE, sum);

    // increment column index
    mv.visitIincInsn(colIndex, 1);

    // .. and check to see if we have any columns left
    mv.visitVarInsn(ILOAD, colIndex);
    mv.visitVarInsn(ILOAD, numColumns);

    mv.visitJumpInsn(IF_ICMPNE, loopHead);

    // otherwise, we're done!
    mv.visitVarInsn(ALOAD, resultArray);
    mv.visitInsn(ARETURN);

  }
}
