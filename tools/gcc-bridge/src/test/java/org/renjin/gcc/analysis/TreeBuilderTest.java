package org.renjin.gcc.analysis;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.ins.GimpleAssign;
import org.renjin.gcc.gimple.ins.GimpleReturn;
import org.renjin.gcc.gimple.type.GimpleIntegerType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TreeBuilderTest {
  

  @Test
  public void trivial() {

    GimpleFunction function = new GimpleFunction();
    GimpleIntegerType int32 = new GimpleIntegerType(32);
    GimpleVarDecl temp1 = function.addVarDecl(int32);
    GimpleVarDecl temp2 = function.addVarDecl(int32);
    GimpleVarDecl temp3 = function.addVarDecl(int32);
    
    // t1 = 1
    // t2 = 2
    // t3 = t1 + t2
    // return t3
    GimpleBasicBlock basicBlock = new GimpleBasicBlock(
            new GimpleAssign(GimpleOp.INTEGER_CST, 
                temp1.newRef(), 
                new GimpleIntegerConstant(int32, 1)),
            new GimpleAssign(GimpleOp.INTEGER_CST,
                temp2.newRef(),
                new GimpleIntegerConstant(int32, 2)),
            new GimpleAssign(GimpleOp.PLUS_EXPR, 
                temp3.newRef(),
                temp1.newRef(),
                temp2.newRef()),
            new GimpleReturn(temp3.newRef()));
    
    function.getBasicBlocks().add(basicBlock);
    
    TreeBuilder builder = new TreeBuilder();
    builder.buildTrees(function, basicBlock);
    
    // we should have one return statement, with all expressions nested
    assertThat(basicBlock.getInstructions().size(), equalTo(1));
    
    // all the local variables should be removed
    assertThat(function.getVariableDeclarations().size(), equalTo(0));
  }
}