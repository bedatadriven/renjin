package org.renjin.compiler;

import com.google.common.collect.Maps;

import edu.uci.ics.jung.graph.util.Context;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.*;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Map;

public class ByteCodeVisitor implements StatementVisitor, ExpressionVisitor, Opcodes {
  
  private GenerationContext generationContext;
  private MethodVisitor mv;
  private Map<LValue, Integer> variableSlots = Maps.newHashMap();
  private Map<IRLabel, Label> labels = Maps.newHashMap();
  
  private int work1;
  private int localVariablesStart;
  
  
  public ByteCodeVisitor(GenerationContext generationContext, MethodVisitor mv) {
    super();
    this.generationContext = generationContext;
    this.mv = mv;
    this.work1 = generationContext.getFirstFreeLocalVariable();
    this.localVariablesStart = work1 + 1;
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
    loadContext();
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/eval/Context", "getEnvironment", "()Lorg/renjin/sexp/Environment;");
    mv.visitLdcInsn(name.getPrintName());
    mv.visitMethodInsn(INVOKESTATIC, "org/renjin/sexp/Symbol", "get", "(Ljava/lang/String;)Lorg/renjin/sexp/Symbol;");
    
    rhs.accept(this);

    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/Environment", "setVariable", "(Lorg/renjin/sexp/Symbol;Lorg/renjin/sexp/SEXP;)V");
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
      loadEnvironment();
      loadContext();
      mv.visitLdcInsn(((Symbol)call.getFunctionSexp()).getPrintName());
      mv.visitMethodInsn(INVOKESTATIC, "org/renjin/sexp/Symbol", "get", "(Ljava/lang/String;)Lorg/renjin/sexp/Symbol;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/Environment", "findFunctionOrThrow", "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Symbol;)Lorg/renjin/sexp/Function;");
    } else {
      // otherwise we need to evaluate the function
      call.getFunction().accept(this);    
    }
       
    Label finish = new Label();

    
    // construct a new PairList with the argument
    Label builtinCall = new Label();
    mv.visitInsn(DUP);
    mv.visitTypeInsn(INSTANCEOF, "org/renjin/sexp/BuiltinFunction");
    mv.visitJumpInsn(IFNE, builtinCall);
    
    Label closureCall = new Label();
    mv.visitInsn(DUP);
    mv.visitTypeInsn(INSTANCEOF, "org/renjin/sexp/Closure");
    mv.visitJumpInsn(IFNE, closureCall);
    
    // APPLY SPECIAL
    applySpecialDynamically(call);
    mv.visitJumpInsn(GOTO, finish);
    
    // APPLY closure
    mv.visitLabel(closureCall);
    applyClosureDynamically(call);
    mv.visitJumpInsn(GOTO, finish);
        
    // APPLY builtin 
    mv.visitLabel(builtinCall);
    applyBuiltinDynamically(call);
  
    mv.visitLabel(finish);
    
  }
  
  private void applySpecialDynamically(DynamicCall call) {
    // here we just send the arguments untouched as SEXPs
    // to the function. 
    // 
    // For the most part, we try to translate R's special
    // functions like `if`, `while`, `for` etc to JVM control
    // structure, but it is still possible for them to be called
    // dynamically so we need to be prepared.
    
    loadContext();
    loadEnvironment();
    pushSexp(call.getSExpression());
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/FunctionCall", "getArguments", "()Lorg/renjin/sexp/PairList;");
    mv.visitMethodInsn(INVOKEINTERFACE, "org/renjin/sexp/Function", "apply",
        "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;Lorg/renjin/sexp/FunctionCall;Lorg/renjin/sexp/PairList;)Lorg/renjin/sexp/SEXP;");
  }

  private void loadEnvironment() {
    mv.visitVarInsn(ALOAD, generationContext.getEnvironmentLdc());
  }

  private void applyClosureDynamically(DynamicCall call) {
    
    mv.visitTypeInsn(CHECKCAST, "org/renjin/sexp/Closure");       
    loadContext();
    loadEnvironment();
    
    pushSexp(call.getSExpression());
    
    // build the pairlist of promises
    mv.visitTypeInsn(NEW, "org/renjin/sexp/PairList$Builder");
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/PairList$Builder", "<init>", "()V");
  
    for(int i=0;i!=call.getArguments().size();++i) {
      Expression argument = call.getArguments().get(i);
      if(argument == Elipses.INSTANCE) {
        loadElipses();
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/PairList$Builder", "addAll", 
        "(Lorg/renjin/sexp/PairList;)Lorg/renjin/sexp/PairList$Builder;");
      } else {
        
        if(call.getArgumentNames().get(i)!=null) {
          mv.visitLdcInsn(call.getArgumentNames().get(i));
        }
        
        if(argument instanceof IRThunk) {
          if(argument.getSExpression() instanceof Symbol) {
            Symbol symbol = (Symbol) argument.getSExpression();
            // create a promise to a variable in this scope
            mv.visitTypeInsn(NEW, "org/renjin/compiler/runtime/VariablePromise");
            mv.visitInsn(DUP);
            loadContext();
            mv.visitLdcInsn(symbol.getPrintName());
            mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/compiler/runtime/VariablePromise", "<init>", 
                "(Lorg/renjin/eval/Context;Ljava/lang/String;)V");
            
          } else {
            // instantatiate our compiled thunk class
            String thunkClass = generationContext.getThunkMap().getClassName((IRThunk)argument);
            mv.visitTypeInsn(NEW, thunkClass);
            mv.visitInsn(DUP);
            loadContext();
            loadEnvironment();
            mv.visitMethodInsn(INVOKESPECIAL, thunkClass , "<init>", 
                "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;)V");
            
          }
        } else {
          argument.accept(this);
        }
        
        if(call.getArgumentNames().get(i)!=null) {
          mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/PairList$Builder", "add", 
          "(Ljava/lang/String;Lorg/renjin/sexp/SEXP;)Lorg/renjin/sexp/PairList$Builder;");
        } else { 
          mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/PairList$Builder", "add", 
              "(Lorg/renjin/sexp/SEXP;)Lorg/renjin/sexp/PairList$Builder;");
        }
      }
    }
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/PairList$Builder", "build", "()Lorg/renjin/sexp/PairList;");    
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/Closure", "matchAndApply",
        "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;Lorg/renjin/sexp/FunctionCall;Lorg/renjin/sexp/PairList;)Lorg/renjin/sexp/SEXP;");
   
  }


  private void loadContext() {
    mv.visitVarInsn(ALOAD, generationContext.getContextLdc());
  }

  private void applyBuiltinDynamically(DynamicCall call) {
    mv.visitTypeInsn(CHECKCAST, "org/renjin/sexp/BuiltinFunction");
    
    maybeStoreElipses(call);

    loadContext();
    loadEnvironment();
    pushSexp(call.getCall());
    pushArgNames(call);
    maybeSpliceArgumentNames(call);
    
    if(call.hasElipses()) {
      loadContext();
      pushEvaluatedArgs(call);      
      spliceArgumentValues(call);
    } else {
      pushEvaluatedArgs(call);      
    }

    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/BuiltinFunction", "apply", 
        "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;Lorg/renjin/sexp/FunctionCall;[Ljava/lang/String;[Lorg/renjin/sexp/SEXP;)Lorg/renjin/sexp/SEXP;");
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
      mv.visitMethodInsn(INVOKESTATIC, "org/renjin/compiler/runtime/CompiledRuntime", "spliceArgNames",
            "([Ljava/lang/String;Lorg/renjin/sexp/PairList;I)[Ljava/lang/String;");
    }
  }

  private void pushSexp(FunctionCall call) {
    generationContext.getSexpPool().pushSexp(mv, call, "Lorg/renjin/sexp/FunctionCall;");
  }

  /**
   * If our dynamic call resolves to a primitive at runtime, then
   * evaluate the arguments inline to avoid the overhead of creating 
   * thunks.
   */
  private void pushEvaluatedArgs(DynamicCall call) {

    // create array
    pushInt(call.getArguments().size());
    mv.visitTypeInsn(ANEWARRAY, "org/renjin/sexp/SEXP");
    
    for(int i=0; i!=call.getArguments().size();++i) {
      if(call.getArguments().get(i) != Elipses.INSTANCE) {
        // keep the array on the stack
        mv.visitInsn(DUP);
        pushInt(i);
    
        Expression arg = call.getArguments().get(i);
        if(arg instanceof IRThunk) {
          SEXP sexp = ((IRThunk) arg).getSEXP();
          if(sexp instanceof Symbol) {
            // since this is a simple case, just do it inline
            visitEnvironmentVariable(new EnvironmentVariable((Symbol) sexp));
          } else {
            // otherwise call out to the corresponding thunk's
            // static method. We rely on the jvm to inline at runtime 
            // if necessary
            loadContext();
            loadEnvironment();
            
            String thunkClass = generationContext.getThunkMap().getClassName((IRThunk)arg);
            mv.visitMethodInsn(INVOKESTATIC, thunkClass , "doEval", 
                "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;)Lorg/renjin/sexp/SEXP;");
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
    
    mv.visitMethodInsn(INVOKEINTERFACE, "org/renjin/sexp/Vector", "getElementAsSEXP", "(I)Lorg/renjin/sexp/SEXP;");    
  }

  @Override
  public void visitEnvironmentVariable(EnvironmentVariable variable) {
    loadEnvironment();
    mv.visitLdcInsn(variable.getName().getPrintName());
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/Environment", "findVariableOrThrow", "(Ljava/lang/String;)Lorg/renjin/sexp/SEXP;");    
    // ensure that promises are forced
    loadContext();
    mv.visitMethodInsn(INVOKEINTERFACE, "org/renjin/sexp/SEXP", "force", "(Lorg/renjin/eval/Context;)Lorg/renjin/sexp/SEXP;");

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
    loadEnvironment();
    mv.visitMethodInsn(INVOKESPECIAL, closureClass, "<init>", "(Lorg/renjin/sexp/Environment;)V");
  }

  @Override
  public void visitPrimitiveCall(PrimitiveCall call) {

    loadContext();
    loadEnvironment();
    
    // push the original function call on the stack
    pushSexp(call.getSExpression());

    // retrieve the value of '...' from the environment, which
    // will contain a pair list of promises

    maybeStoreElipses(call);

    // push the argument names
    pushArgNames(call);
    maybeSpliceArgumentNames(call);
    
    // push the argument values
    
    if(call.hasElipses()) {
      loadContext();
      pushPrimitiveArgArray(call);
      spliceArgumentValues(call);      
    } else {
      pushPrimitiveArgArray(call);
    }
    
    mv.visitMethodInsn(INVOKESTATIC, call.getWrapperClass().getName().replace('.', '/'), "doApply",
        "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;Lorg/renjin/sexp/FunctionCall;[Ljava/lang/String;[Lorg/renjin/sexp/SEXP;)Lorg/renjin/sexp/SEXP;");
  }

  private void spliceArgumentValues(CallExpression call) {

    
    // insert the elipses argument values 
    mv.visitVarInsn(ALOAD, work1); // '...' pairlist
    pushInt(call.getElipsesIndex());
    mv.visitMethodInsn(INVOKESTATIC, "org/renjin/compiler/runtime/CompiledRuntime", "spliceArgValues",
          "(Lorg/renjin/eval/Context;[Lorg/renjin/sexp/SEXP;Lorg/renjin/sexp/PairList;I)[Lorg/renjin/sexp/SEXP;");
  
  }

  private void maybeStoreElipses(CallExpression call) {
    if(call.hasElipses()) {
      loadElipses();
      mv.visitVarInsn(ASTORE, work1);
    }
  }

  private void loadElipses() {
    loadEnvironment();
    mv.visitFieldInsn(GETSTATIC, "org/renjin/sexp/Symbols", "ELLIPSES", "Lorg/renjin/sexp/Symbol;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/Environment", "getVariable", "(Lorg/renjin/sexp/Symbol;)Lorg/renjin/sexp/SEXP;");
    mv.visitTypeInsn(CHECKCAST, "org/renjin/sexp/PairList");
  }
  
  private void pushPrimitiveArgArray(PrimitiveCall call) {
    // create array of values
    pushInt(call.getArguments().size());
    mv.visitTypeInsn(ANEWARRAY, "org/renjin/sexp/SEXP");
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
    mv.visitMethodInsn(INVOKEINTERFACE, "org/renjin/sexp/SEXP", "length", "()I");
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
      
      mv.visitMethodInsn(INVOKESTATIC, "org/renjin/compiler/runtime/CompiledRuntime", 
            "evaluateCondition", "(Lorg/renjin/sexp/SEXP;)Z");
      
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
