package org.renjin.compiler.pipeline;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.pipeline.accessor.Accessor;
import org.renjin.compiler.pipeline.accessor.Accessors;
import org.renjin.compiler.pipeline.accessor.InputGraph;

import static org.objectweb.asm.Opcodes.*;

public class RowMeanJitter implements FunctionJitter  {

  @Override
  public void compute(ComputeMethod method, DeferredNode node) {

    InputGraph inputGraph = new InputGraph(node);

    Accessor matrix = Accessors.create(node.getOperand(0), inputGraph);
    matrix.init(method);

    int meansLocal = method.reserveLocal(1);
    Accessor numRows = Accessors.create(node.getOperand(1), inputGraph);
    numRows.init(method);

    MethodVisitor mv = method.getVisitor();
    int numRowsLocal = method.reserveLocal(meansLocal);
    int rowLocal = method.reserveLocal(1);
    int counterLocal = method.reserveLocal(1);

    mv.visitInsn(ICONST_0);
    numRows.pushInt(method);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, numRowsLocal);

    // create array (size still on stack)
    mv.visitIntInsn(NEWARRAY, T_DOUBLE);
    mv.visitVarInsn(ASTORE, meansLocal);

    // initialize counter
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, rowLocal);

    // initialize row index
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, counterLocal);

    // check whether to loop
    Label l4 = new Label();
    mv.visitLabel(l4);
    mv.visitVarInsn(ILOAD, counterLocal);
    matrix.pushLength(method);

    Label l5 = new Label();
    mv.visitJumpInsn(IF_ICMPEQ, l5);

    Label l6 = new Label();
    mv.visitLabel(l6);
    mv.visitVarInsn(ALOAD, meansLocal);
    mv.visitVarInsn(ILOAD, rowLocal);
    mv.visitInsn(DUP2);

    // load the current row sum at index rowLocal onto the stack
    mv.visitInsn(DALOAD);

    // load the next value onto the stack
    mv.visitVarInsn(ILOAD, counterLocal);
    matrix.pushDouble(method);

    // add to sum
    mv.visitInsn(DADD);
    mv.visitInsn(DASTORE);

    Label l7 = new Label();
    mv.visitLabel(l7);
    mv.visitIincInsn(rowLocal, 1);

    // check if we've hit the end of the rows
    // and need to start again
    Label l8 = new Label();
    mv.visitLabel(l8);
    mv.visitVarInsn(ILOAD, rowLocal);
    mv.visitVarInsn(ILOAD, numRowsLocal);
    Label l9 = new Label();
    mv.visitJumpInsn(IF_ICMPNE, l9);
    Label l10 = new Label();
    mv.visitLabel(l10);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, rowLocal);

    // increment the vector index counter and loop
    mv.visitLabel(l9);
    mv.visitIincInsn(counterLocal, 1);
    mv.visitJumpInsn(GOTO, l4);

    mv.visitLabel(l5);

    int numColsLocal = method.reserveLocal(2);
    // calculate num cols (length / num rows)
    matrix.pushLength(method);
    mv.visitVarInsn(ILOAD, numRowsLocal);
    mv.visitInsn(IDIV);
    mv.visitInsn(I2D);
    mv.visitVarInsn(DSTORE, numColsLocal);

    // init the second loop to
    // divide out the means

    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, counterLocal);

    // check loop
    Label l11 = new Label();
    mv.visitLabel(l11);
    mv.visitVarInsn(ILOAD, counterLocal);
    mv.visitVarInsn(ILOAD, numRowsLocal);
    Label l12 = new Label();
    mv.visitJumpInsn(IF_ICMPEQ, l12);


    Label l13 = new Label();
    mv.visitLabel(l13);
    mv.visitVarInsn(ALOAD, meansLocal);
    mv.visitVarInsn(ILOAD, counterLocal);
    mv.visitInsn(DUP2);
    // load the means[i] onto stack
    mv.visitInsn(DALOAD);
    mv.visitVarInsn(DLOAD, numColsLocal);

    mv.visitInsn(DDIV);

    // store back into means[]
    mv.visitInsn(DASTORE);
    Label l14 = new Label();

    mv.visitLabel(l14);
    mv.visitIincInsn(counterLocal, 1);
    mv.visitJumpInsn(GOTO, l11);
    mv.visitLabel(l12);

    mv.visitVarInsn(ALOAD, meansLocal);
    mv.visitInsn(ARETURN);
  }
}
