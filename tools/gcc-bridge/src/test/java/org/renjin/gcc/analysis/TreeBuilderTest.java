package org.renjin.gcc.analysis;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleMemRef;
import org.renjin.gcc.gimple.expr.GimpleParamRef;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleReturn;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePointerType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

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
            new GimpleAssignment(GimpleOp.INTEGER_CST, 
                temp1.newRef(), 
                new GimpleIntegerConstant(int32, 1)),
            new GimpleAssignment(GimpleOp.INTEGER_CST,
                temp2.newRef(),
                new GimpleIntegerConstant(int32, 2)),
            new GimpleAssignment(GimpleOp.PLUS_EXPR, 
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
  
  @Test
  public void variablesUsedTwiceInSameExpressionArePreserved() {

    GimpleFunction function = new GimpleFunction();
    GimpleIntegerType int32 = new GimpleIntegerType(32);
    GimpleVarDecl temp1 = function.addVarDecl(int32);
    GimpleVarDecl temp2 = function.addVarDecl(int32);
    
    // temp1 = 42
    // temp2 = temp * temp
    
    GimpleBasicBlock basicBlock = new GimpleBasicBlock(
        new GimpleAssignment(GimpleOp.INTEGER_CST,
            temp1.newRef(),
            new GimpleIntegerConstant(int32, 42)),
        new GimpleAssignment(GimpleOp.MULT_EXPR,
            temp2.newRef(),
            temp1.newRef(),
            temp1.newRef()));
    
    function.getBasicBlocks().add(basicBlock);

    TreeBuilder builder = new TreeBuilder();
    builder.buildTrees(function, basicBlock);
    
    // Verify that we've preserved temp1
    // In this simple case we actually could use the DUP instruction to avoid "spilling" 
    // the value of temp1 to a local variable, but that's more complex than we want
    // to deal with at this point.
    
    assertThat(function.getVariableDeclarations().size(), equalTo(2));
  }
  
  @Test
  public void variablesUsedTwiceInLhsArePreserved() {

    GimpleFunction function = new GimpleFunction();
    GimpleIntegerType int32 = new GimpleIntegerType(32);
    GimplePointerType pint32 = new GimplePointerType(int32);

    GimpleParamRef param1 = new GimpleParamRef(99, "param1");

    GimpleVarDecl t1 = function.addVarDecl(int32);
    GimpleVarDecl t2 = function.addVarDecl(int32);
    GimpleVarDecl p1 = function.addVarDecl(pint32);

    // t1 = 8
    // p1 = param1 + 8
    // t2 = *p1
    // *p1 = param1

    GimpleBasicBlock basicBlock = new GimpleBasicBlock(
        new GimpleAssignment(GimpleOp.INTEGER_CST,
            t1.newRef(),
            new GimpleIntegerConstant(int32, 8)),
        new GimpleAssignment(GimpleOp.POINTER_PLUS_EXPR,
            p1.newRef(),
            param1,
            t1.newRef()),
        new GimpleAssignment(GimpleOp.MEM_REF,
            t2.newRef(),
            new GimpleMemRef(p1.newRef())),
        new GimpleAssignment(GimpleOp.VAR_DECL,
            new GimpleMemRef(p1.newRef()),
            new GimpleIntegerConstant(int32, 0)));

    System.out.println(basicBlock);
    
    function.getBasicBlocks().add(basicBlock);


    TreeBuilder builder = new TreeBuilder();
    builder.buildTrees(function, basicBlock);

    System.out.println(basicBlock);
    
    // Verify that p1 has been preserved -- it has been used twice, albeit once
    // on the left hand side, and once on the right
    assertTrue("p2 is preserved", function.getVariableDeclarations().contains(p1));
    assertFalse("t1 is eliminated", function.getVariableDeclarations().contains(t1));
  }
}