package r.compiler.ir;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import r.compiler.ir.ssa.PhiFunction;
import r.compiler.ir.ssa.SsaVariable;
import r.compiler.ir.tac.expressions.CmpGE;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.DynamicCall;
import r.compiler.ir.tac.expressions.ElementAccess;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.ExpressionVisitor;
import r.compiler.ir.tac.expressions.Increment;
import r.compiler.ir.tac.expressions.LValue;
import r.compiler.ir.tac.expressions.LocalVariable;
import r.compiler.ir.tac.expressions.MakeClosure;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.expressions.Temp;
import r.compiler.ir.tac.statements.Assignment;
import r.compiler.ir.tac.statements.ExprStatement;
import r.compiler.ir.tac.statements.GotoStatement;
import r.compiler.ir.tac.statements.IfStatement;
import r.compiler.ir.tac.statements.ReturnStatement;
import r.compiler.ir.tac.statements.StatementVisitor;
import r.lang.SEXP;
import r.lang.Symbol;

public class ByteCodeVisitor implements StatementVisitor, ExpressionVisitor, Opcodes {
  
  private MethodVisitor mv;
  private Map<LValue, Integer> variableSlots;
  
  private int context = 1;
  private int rho = 2;
  
  
  public class GenerationContext {
    
    
  }
  
  
  public ByteCodeVisitor(MethodVisitor mv) {
    super();
    this.mv = mv;
  }

  
  
  @Override
  public void visitAssignment(Assignment assignment) {
    LValue lhs = assignment.getLHS();
    if(lhs instanceof EnvironmentVariable) {
      environmentAssignment(((EnvironmentVariable)lhs).getName(), 
          assignment.getRHS());
    } else {
      localVariableAssignment(lhs, assignment.getRHS());
    }
  }


  /**
   * Assigns a value into a local variable slot
   */
  private void localVariableAssignment(LValue lhs, Expression rhs) {
    
    rhs.accept(this);
    
    mv.visitVarInsn(ASTORE, getLocalVariablePos(lhs));
  }

  private int getLocalVariablePos(LValue lhs) {
    return 0;
  }

  /**
   * Assign a value into the context's {@code Environment} 
   */
  private void environmentAssignment(Symbol name, Expression rhs) {
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Context", "getEnvironment", "()Lr/lang/Environment;");
    mv.visitLdcInsn(name.getPrintName());
    mv.visitMethodInsn(INVOKESTATIC, "r/lang/Symbol", "get", "(Ljava/lang/String;)Lr/lang/Symbol;");
    
    rhs.accept(this);

    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "setVariable", "(Lr/lang/Symbol;Lr/lang/SEXP;)V");
  }



  @Override
  public void visitConstant(Constant constant) {
    if(constant.getValue() instanceof SEXP) {
      SEXP exp = (SEXP)constant.getValue();
      exp.accept(new ConstantGeneratingVisitor(mv));
    } else {
      throw new UnsupportedOperationException();
    }
    
  }



  @Override
  public void visitDynamicCall(DynamicCall call) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitElementAccess(ElementAccess expr) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitEnvironmentVariable(EnvironmentVariable variable) {
    mv.visitVarInsn(ALOAD, rho);
    mv.visitLdcInsn("x");
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "getVariable", "(Ljava/lang/String;)Lr/lang/SEXP;");    
  }



  @Override
  public void visitIncrement(Increment increment) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitLocalVariable(LocalVariable variable) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitMakeClosure(MakeClosure closure) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitPrimitiveCall(PrimitiveCall call) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitTemp(Temp temp) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitCmpGE(CmpGE cmp) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitSsaVariable(SsaVariable variable) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitPhiFunction(PhiFunction phiFunction) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitExprStatement(ExprStatement statement) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitGoto(GotoStatement statement) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitIf(IfStatement ifStatement) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitReturn(ReturnStatement returnStatement) {
   
    returnStatement.getValue().accept(this);
    
    mv.visitInsn(ARETURN);
    
  }

  
}
