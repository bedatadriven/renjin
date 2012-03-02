package r.compiler.ir;

import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import r.compiler.cfg.BasicBlock;
import r.compiler.ir.ssa.PhiFunction;
import r.compiler.ir.ssa.SsaVariable;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.CmpGE;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.DynamicCall;
import r.compiler.ir.tac.expressions.ElementAccess;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.ExpressionVisitor;
import r.compiler.ir.tac.expressions.Length;
import r.compiler.ir.tac.expressions.UnevaluatedArgument;
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
import r.jvmi.wrapper.WrapperGenerator;
import r.lang.SEXP;
import r.lang.Symbol;

import com.google.common.collect.Maps;

public class ByteCodeVisitor implements StatementVisitor, ExpressionVisitor, Opcodes {
  
  private MethodVisitor mv;
  private Map<LValue, Integer> variableSlots = Maps.newHashMap();
  private Map<IRLabel, Label> labels = Maps.newHashMap();
  
  private int context = 1;
  private int rho = 2;
  private int localVariablesStart = 3;
  
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
    
    if(rhs instanceof Increment) {
      Increment inc = (Increment) rhs;
      if(inc.getCounter().equals(lhs)) {
        mv.visitIincInsn(getVariableSlot(lhs), 1);
        return;
      }
    } else if(rhs instanceof Constant ) {
      // need to generalize this to accommodate primitive results from
      // methods as well.
      // the following just handles the special case of the integer
      // loop counter for _for_ loops.
      
      Constant constant = (Constant) rhs;
      
      constant.accept(this);
      
      if(constant.getValue() instanceof Integer) {
        mv.visitVarInsn(ISTORE, getVariableSlot(lhs));
      } else {
        mv.visitVarInsn(ASTORE, getVariableSlot(lhs));
      }
    } else if(rhs instanceof Length) {
      rhs.accept(this);
      mv.visitVarInsn(ISTORE, getVariableSlot(lhs));
      
    } else {
      
      rhs.accept(this);
  
      mv.visitVarInsn(ASTORE, getVariableSlot(lhs));
    }
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

  public void startBasicBlock(BasicBlock bb) {
    if(bb.getLabel() != null) {
      mv.visitLabel(getAsmLabel(bb.getLabel()));
    }
  }

  @Override
  public void visitConstant(Constant constant) {
    if(constant.getValue() instanceof SEXP) {
      SEXP exp = (SEXP)constant.getValue();
      exp.accept(new ConstantGeneratingVisitor(mv));
    } else if (constant.getValue() instanceof Integer) {
      pushInt((Integer)constant.getValue());
    } else {
      throw new UnsupportedOperationException();
    }
    
  }



  @Override
  public void visitDynamicCall(DynamicCall call) {

    EnvironmentVariable functionName = (EnvironmentVariable)call.getFunction();
    
    // find the function
    
    mv.visitVarInsn(ALOAD, rho);
    mv.visitLdcInsn(functionName.getName().getPrintName());
    mv.visitMethodInsn(INVOKESTATIC, "r/lang/Symbol", "get", "(Ljava/lang/String;)Lr/lang/Symbol;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "findFunction", "(Lr/lang/Symbol;)Lr/lang/Function;");
    
    // check if null
    Label l2 = new Label();
    mv.visitInsn(DUP);
    mv.visitJumpInsn(IFNONNULL, l2);
    mv.visitTypeInsn(NEW, "r/lang/exception/EvalException");
    mv.visitInsn(DUP);
    mv.visitLdcInsn("No such method");
    mv.visitInsn(ICONST_0);
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    mv.visitMethodInsn(INVOKESPECIAL, "r/lang/exception/EvalException", "<init>", "(Ljava/lang/String;[Ljava/lang/Object;)V");
    mv.visitInsn(ATHROW);
    
    
    // check if strict
    Label l3 = new Label();
    mv.visitLabel(l2);
    mv.visitInsn(DUP);
    mv.visitTypeInsn(INSTANCEOF, "r/lang/StrictPrimitiveFunction");
    mv.visitJumpInsn(IFNE, l3);

    mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
    mv.visitInsn(DUP);
    mv.visitLdcInsn("non-strict primitives not yet implemented");
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V");
    mv.visitInsn(ATHROW);
    
    // execute strict
    mv.visitLabel(l3);
    mv.visitVarInsn(ALOAD, context);
    mv.visitVarInsn(ALOAD, rho);
    pushArgs(call);
    
    mv.visitMethodInsn(INVOKEINTERFACE, "r/lang/StrictPrimitiveFunction", "applyStrict", "(Lr/lang/Context;Lr/lang/Environment;[Lr/lang/SEXP;)Lr/lang/SEXP;");    
  }

  private void pushArgs(DynamicCall call) {

    // create array
    pushInt(call.getArguments().size());
    mv.visitTypeInsn(ANEWARRAY, "r/lang/SEXP");
    
    for(int i=0; i!=call.getArguments().size();++i) {
      mv.visitInsn(DUP);
      
      pushInt(i);
      call.getArguments().get(i).accept(this);
      mv.visitInsn(AASTORE);
    }
  }

  private void pushInt(int i) {
    if(i <= 5) {
      mv.visitInsn(ICONST_0 + i);
    } else if(i < 127){
      mv.visitIntInsn(BIPUSH, i);
    } else {
      throw new UnsupportedOperationException("more than 127 arguments? something is wrong.");
    }
  }

  @Override
  public void visitElementAccess(ElementAccess expr) {
    expr.getVector().accept(this);
    expr.getIndex().accept(this);
    
    mv.visitMethodInsn(INVOKEINTERFACE, "r/lang/Vector", "getElementAsSEXP", "(I)Lr/lang/SEXP;");    
  }

  @Override
  public void visitEnvironmentVariable(EnvironmentVariable variable) {
    mv.visitVarInsn(ALOAD, rho);
    mv.visitLdcInsn(variable.getName().getPrintName());
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "getVariable", "(Ljava/lang/String;)Lr/lang/SEXP;");    
  }

  @Override
  public void visitIncrement(Increment increment) {
    throw new UnsupportedOperationException();
    
  }



  @Override
  public void visitLocalVariable(LocalVariable variable) {
    mv.visitVarInsn(ILOAD, getVariableSlot(variable));    
  }



  @Override
  public void visitMakeClosure(MakeClosure closure) {
    throw new UnsupportedOperationException();    
  }

  @Override
  public void visitPrimitiveCall(PrimitiveCall call) {

    mv.visitVarInsn(ALOAD, context);
    mv.visitVarInsn(ALOAD, rho);
    
    for(Expression arg : call.getArguments()) {
      arg.accept(this);
    }
    
    StringBuilder methodSig = new StringBuilder();
    methodSig.append("(Lr/lang/Context;Lr/lang/Environment;");
    for(int i=0;i!=call.getArguments().size();++i) {
      methodSig.append("Lr/lang/SEXP;");
    }
    methodSig.append(")Lr/lang/SEXP;");
   
    
    mv.visitMethodInsn(INVOKESTATIC, WrapperGenerator.toFullJavaName(call.getName().getPrintName()).replace(".", "/"), "doApply", methodSig.toString());    
  }
  
  @Override
  public void visitLength(Length length) {
    length.getVector().accept(this);
    mv.visitMethodInsn(INVOKEINTERFACE, "r/lang/SEXP", "length", "()I");
  }

  @Override
  public void visitTemp(Temp temp) {
    mv.visitVarInsn(ALOAD, getVariableSlot(temp));
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
    mv.visitJumpInsn(GOTO, getAsmLabel(statement.getTarget()));
  }



  @Override
  public void visitIf(IfStatement stmt) {
    
    if(stmt.getCondition() instanceof CmpGE) {
     
      CmpGE cmp = (CmpGE) stmt.getCondition();
      mv.visitVarInsn(ILOAD, getVariableSlot((LValue)cmp.getOp1()));
      mv.visitVarInsn(ILOAD, getVariableSlot((LValue)cmp.getOp2()));
     
      mv.visitJumpInsn(IF_ICMPLT, getAsmLabel(stmt.getFalseTarget()));
      
    } else {
    
      stmt.getCondition().accept(this);
      
      mv.visitMethodInsn(INVOKESTATIC, "r/compiler/CompiledRuntime", 
            "evaluateCondition", "(Lr/lang/SEXP;)Z");
      
      // IFEQ : jump if i==0
      mv.visitJumpInsn(IFEQ, getAsmLabel(stmt.getFalseTarget()));
    }
    mv.visitJumpInsn(GOTO, getAsmLabel(stmt.getTrueTarget()));
  }
  
  private Label getAsmLabel(IRLabel label) {
    Label asmLabel = labels.get(label);
    if(asmLabel == null) {
      asmLabel = new Label();
      labels.put(label, asmLabel);
    }
    return asmLabel;
  }
  
  private int getVariableSlot(LValue lvalue) {
    Integer index = variableSlots.get(lvalue);
    if(index == null) {
      index = variableSlots.size();
      variableSlots.put(lvalue, index);
    }
    return index + localVariablesStart;
  }


  @Override
  public void visitReturn(ReturnStatement returnStatement) {
   
    returnStatement.getValue().accept(this);
    
    mv.visitInsn(ARETURN);
    
  }
  
  public void dumpLdc() {
    for(LValue var : variableSlots.keySet()) {
      System.out.println((variableSlots.get(var) + localVariablesStart) + " => " + var);
    }
  }



  @Override
  public void visitPromise(UnevaluatedArgument promise) {
    // TODO Auto-generated method stub
    
  }



}
