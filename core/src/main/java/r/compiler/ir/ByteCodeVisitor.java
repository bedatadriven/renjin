package r.compiler.ir;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.LValue;
import r.compiler.ir.tree.AssignmentNode;
import r.compiler.ir.tree.ClosureNode;
import r.compiler.ir.tree.DynamicCallNode;
import r.compiler.ir.tree.GetElementNode;
import r.compiler.ir.tree.PrimitiveCallNode;
import r.compiler.ir.tree.TreeNode;
import r.compiler.ir.tree.TreeVisitor;
import r.compiler.ir.tree.ValueNode;
import r.lang.Symbol;

public class ByteCodeVisitor extends TreeVisitor implements Opcodes {
  
  private MethodVisitor mv;
  private Map<LValue, Integer> variableSlots;

  public ByteCodeVisitor(MethodVisitor mv) {
    super();
    this.mv = mv;
  }

  @Override
  public final void assignment(AssignmentNode assignmentNode) {
    LValue lhs = assignmentNode.getLHS();
    if(lhs instanceof EnvironmentVariable) {
      environmentAssignment(((EnvironmentVariable)lhs).getName(), 
          assignmentNode.getRHS());
    } else {
      localVariableAssignment(lhs, assignmentNode.getRHS());
    }
  }

  /**
   * Assigns a value into a local variable slot
   */
  private void localVariableAssignment(LValue lhs, TreeNode rhs) {
    
    rhs.accept(this);
    
    mv.visitVarInsn(ASTORE, getLocalVariablePos(lhs));
  }

  private int getLocalVariablePos(LValue lhs) {
    return 0;
  }

  /**
   * Assign a value into the context's {@code Environment} 
   */
  private void environmentAssignment(Symbol name, TreeNode rhs) {
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Context", "getEnvironment", "()Lr/lang/Environment;");
    mv.visitLdcInsn(name.getPrintName());
    mv.visitMethodInsn(INVOKESTATIC, "r/lang/Symbol", "get", "(Ljava/lang/String;)Lr/lang/Symbol;");
    
    rhs.accept(this);
    
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "setVariable", "(Lr/lang/Symbol;Lr/lang/SEXP;)V");
  }

  @Override
  public void closure(ClosureNode closureNode) {
    // TODO Auto-generated method stub
    super.closure(closureNode);
  }

  @Override
  public void dynamicCall(DynamicCallNode dynamicCallNode) {
  
  }

  @Override
  public void getElement(GetElementNode getElementNode) {
    // TODO Auto-generated method stub
    super.getElement(getElementNode);
  }

  @Override
  public void primitiveCall(PrimitiveCallNode primitiveCallNode) {
    // TODO Auto-generated method stub
    super.primitiveCall(primitiveCallNode);
  }

  @Override
  public void value(ValueNode valueNode) {
  }
}
