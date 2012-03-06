package r.compiler;

import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import r.compiler.cfg.BasicBlock;
import r.compiler.ir.ssa.PhiFunction;
import r.compiler.ir.ssa.SsaVariable;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.CallExpression;
import r.compiler.ir.tac.expressions.CmpGE;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.DynamicCall;
import r.compiler.ir.tac.expressions.ElementAccess;
import r.compiler.ir.tac.expressions.Elipses;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.ExpressionVisitor;
import r.compiler.ir.tac.expressions.IRThunk;
import r.compiler.ir.tac.expressions.Increment;
import r.compiler.ir.tac.expressions.LValue;
import r.compiler.ir.tac.expressions.Length;
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
import r.lang.FunctionCall;
import r.lang.SEXP;
import r.lang.Symbol;

import com.google.common.collect.Maps;

public class ByteCodeVisitor implements StatementVisitor, ExpressionVisitor, Opcodes {
  
  private GenerationContext generationContext;
  private MethodVisitor mv;
  private Map<LValue, Integer> variableSlots = Maps.newHashMap();
  private Map<IRLabel, Label> labels = Maps.newHashMap();
  
  private int context = 1;
  private int rho = 2;
  private int work1 = 3;
  private int work2 = 4;
  private int localVariablesStart = 5;
  
  
  public ByteCodeVisitor(GenerationContext generationContext, MethodVisitor mv) {
    super();
    this.generationContext = generationContext;
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
    mv.visitVarInsn(ALOAD, context);
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Context", "getEnvironment", "()Lr/lang/Environment;");
    mv.visitLdcInsn(name.getPrintName());
    mv.visitMethodInsn(INVOKESTATIC, "r/lang/Symbol", "get", "(Ljava/lang/String;)Lr/lang/Symbol;");
    
    rhs.accept(this);

    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "setVariable", "(Lr/lang/Symbol;Lr/lang/SEXP;)V");
  }

  public void startBasicBlock(BasicBlock bb) {
    if(bb.getLabels() != null) {
      for(IRLabel label : bb.getLabels()) {
        mv.visitLabel(getAsmLabel(label));        
      }
    }
  }

  @Override
  public void visitConstant(Constant constant) {
    if(constant.getValue() instanceof SEXP) {
      SEXP exp = (SEXP)constant.getValue();
      exp.accept(new ConstantGeneratingVisitor(mv));
    } else if (constant.getValue() instanceof Integer) {
      ByteCodeUtil.pushInt(mv, (Integer)constant.getValue());
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public void visitDynamicCall(DynamicCall call) {
    
    if(call.getFunctionSexp() instanceof Symbol) {
      mv.visitVarInsn(ALOAD, rho);
      mv.visitLdcInsn(((Symbol)call.getFunctionSexp()).getPrintName());
      mv.visitMethodInsn(INVOKESTATIC, "r/lang/Symbol", "get", "(Ljava/lang/String;)Lr/lang/Symbol;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "findFunctionOrThrow", "(Lr/lang/Symbol;)Lr/lang/Function;");
    } else {
      // otherwise we need to evaluate the function
      call.getFunction().accept(this);    
    }
    
    // construct a new PairList with the argument
    Label l3 = new Label();
    mv.visitInsn(DUP);
    mv.visitTypeInsn(INSTANCEOF, "r/lang/BuiltinFunction");
    mv.visitJumpInsn(IFNE, l3);
    
    mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
    mv.visitInsn(DUP);
    mv.visitLdcInsn("non-strict primitives not yet implemented");
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V");
    mv.visitInsn(ATHROW);
    
    // execute strict
    mv.visitLabel(l3);
    mv.visitTypeInsn(CHECKCAST, "r/lang/BuiltinFunction");
    
    maybeLoadElipses(call);
    
    mv.visitVarInsn(ALOAD, context);
    mv.visitVarInsn(ALOAD, rho);
    pushSexp(call.getCall());
    pushArgNames(call);
    maybeSpliceArgumentNames(call);
    
    pushEvaluatedArgs(call);
    maybeSpliceArgumentValues(call);

    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/BuiltinFunction", "apply", 
        "(Lr/lang/Context;Lr/lang/Environment;Lr/lang/FunctionCall;[Ljava/lang/String;[Lr/lang/SEXP;)Lr/lang/SEXP;");
  
    
  }

  /**
   * Pushes the array of argument names onto the stack.
   */
  private void pushArgNames(CallExpression call) {
    
    pushInt(call.getArgumentNames().size());
    mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
   
    for(int i=0;i!=call.getArgumentNames().size();++i) {
      if(call.getArgumentNames().get(i) != null) {
        mv.visitInsn(DUP);
        ByteCodeUtil.pushInt(mv, i);
        mv.visitLdcInsn( call.getArgumentNames().get(i) );
        mv.visitInsn(AASTORE);
      }
    }  
  }

  /**
   * If the call includes an '...' argument, generate the call
   * to splice the remaining arguments to this function into
   * the argument list.
   */
  private void maybeSpliceArgumentNames(CallExpression call) {
    if(call.hasElipses()) {
      // insert the elipses argument names
      mv.visitVarInsn(ALOAD, work1); // '...' pairlist
      pushInt(call.getElipsesIndex());
      mv.visitMethodInsn(INVOKESTATIC, "r/compiler/runtime/CompiledRuntime", "spliceArgNames",
            "([Ljava/lang/String;Lr/lang/PairList;I)[Ljava/lang/String;");
    }
  }

  private void pushSexp(FunctionCall call) {
    mv.visitVarInsn(ALOAD, 0); // this
    mv.visitFieldInsn(GETFIELD, generationContext.getClassName(),
        generationContext.getSexpPool().add(call, "Lr/lang/FunctionCall;"), 
        "Lr/lang/FunctionCall;");
  }

  /**
   * If our dynamic call resolves to a primitive at runtime, then
   * evaluate the arguments inline to avoid the overhead of creating 
   * thunks.
   */
  private void pushEvaluatedArgs(DynamicCall call) {

    // create array
    pushInt(call.getArguments().size());
    mv.visitTypeInsn(ANEWARRAY, "r/lang/SEXP");
    
    for(int i=0; i!=call.getArguments().size();++i) {
      if(call.getArguments().get(i) != Elipses.INSTANCE) {
        // keep the array on the stack
        mv.visitInsn(DUP);
        pushInt(i);
    
        Expression arg = call.getArguments().get(0);
        if(arg instanceof IRThunk) {
          SEXP sexp = ((IRThunk) arg).getSEXP();
          if(sexp instanceof Symbol) {
            // since this is a simple case, just do it inline
            visitEnvironmentVariable(new EnvironmentVariable((Symbol) sexp));
          } else {
            // otherwise call out to the corresponding thunk's
            // static method. We rely on the jvm to inline at runtime 
            // if necessary
            mv.visitVarInsn(ALOAD, context);
            mv.visitVarInsn(ALOAD, rho);
            
            String thunkClass = generationContext.getThunkMap().getClassName((IRThunk)arg);
            mv.visitMethodInsn(INVOKESTATIC, thunkClass , "doEval", 
                "(Lr/lang/Context;Lr/lang/Environment;)Lr/lang/SEXP;");
          }
        } else {
          arg.accept(this);
        }
        mv.visitInsn(AASTORE);
      }
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
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "findVariableOrThrow", "(Ljava/lang/String;)Lr/lang/SEXP;");    
    // ensure that promises are forced
    mv.visitMethodInsn(INVOKEINTERFACE, "r/lang/SEXP", "force", "()Lr/lang/SEXP;");
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
    String closureClass = generationContext.addClosure(closure.getFunction());
    mv.visitTypeInsn(NEW, closureClass);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ALOAD, context);
    mv.visitVarInsn(ALOAD, rho);    
    mv.visitMethodInsn(INVOKESPECIAL, closureClass, "<init>", "(Lr/lang/Context;Lr/lang/Environment;)V");
  }

  @Override
  public void visitPrimitiveCall(PrimitiveCall call) {

    mv.visitVarInsn(ALOAD, context);
    mv.visitVarInsn(ALOAD, rho);

    // push the original function call on the stack
    pushSexp(call.getSExpression());

    // retrieve the value of '...' from the environment, which
    // will contain a pair list of promises

    maybeLoadElipses(call);

    // push the argument names
    pushArgNames(call);
    maybeSpliceArgumentNames(call);
    
    // push the argument values
    pushPrimitiveArgArray(call);
    maybeSpliceArgumentValues(call);
    
    mv.visitMethodInsn(INVOKESTATIC, call.getWrapperClass().getName().replace('.', '/'), "matchAndApply", 
        "(Lr/lang/Context;Lr/lang/Environment;Lr/lang/FunctionCall;[Ljava/lang/String;[Lr/lang/SEXP;)Lr/lang/SEXP;");
  }

  private void maybeSpliceArgumentValues(CallExpression call) {
    if(call.hasElipses()) {
      // insert the elipses argument values 
      mv.visitVarInsn(ALOAD, work1); // '...' pairlist
      pushInt(call.getElipsesIndex());
      mv.visitMethodInsn(INVOKESTATIC, "r/compiler/runtime/CompiledRuntime", "spliceArgValues",
            "([Lr/lang/SEXP;Lr/lang/PairList;I)[Lr/lang/SEXP;");
    }
  }

  private void maybeLoadElipses(CallExpression call) {
    if(call.hasElipses()) {
      mv.visitVarInsn(ALOAD, rho);
      mv.visitFieldInsn(GETSTATIC, "r/lang/Symbols", "ELLIPSES", "Lr/lang/Symbol;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "getVariable", "(Lr/lang/Symbol;)Lr/lang/SEXP;");
      mv.visitTypeInsn(CHECKCAST, "r/lang/PairList");
      mv.visitVarInsn(ASTORE, work1);
    }
  }
  
  private void pushPrimitiveArgArray(PrimitiveCall call) {
    // create array of values
    pushInt(call.getArguments().size());
    mv.visitTypeInsn(ANEWARRAY, "r/lang/SEXP");
    for(int i=0;i!=call.getArguments().size();++i) {
      if(call.getArguments().get(i) != Elipses.INSTANCE) {
        mv.visitInsn(DUP);
        pushInt(i);
        call.getArguments().get(i).accept(this);
        mv.visitInsn(AASTORE);
      }
    }
  }
  
  private void pushInt(int i) {
     ByteCodeUtil.pushInt(mv, i);
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
    statement.getRHS().accept(this);
    mv.visitInsn(POP);
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
      
      mv.visitMethodInsn(INVOKESTATIC, "r/compiler/runtime/CompiledRuntime", 
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
  
  @Override
  public void visitPromise(IRThunk promise) {
    // TODO Auto-generated method stub
    
  }
}
