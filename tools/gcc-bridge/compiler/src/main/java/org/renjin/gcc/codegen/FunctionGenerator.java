/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen;

import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.InvocationStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.GlobalVarAllocator;
import org.renjin.gcc.codegen.var.LocalStaticVarAllocator;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.statement.*;
import org.renjin.gcc.gimple.type.GimpleVoidType;
import org.renjin.gcc.peephole.PeepholeOptimizer;
import org.renjin.gcc.runtime.Stdlib;
import org.renjin.gcc.symbols.LocalVariableTable;
import org.renjin.gcc.symbols.UnitSymbolTable;
import org.renjin.repackaged.asm.*;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.asm.util.Textifier;
import org.renjin.repackaged.asm.util.TraceMethodVisitor;
import org.renjin.repackaged.guava.base.Throwables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static org.renjin.repackaged.asm.Opcodes.*;

/**
 * Generates the bytecode for a {@link GimpleFunction}
 */
public class FunctionGenerator implements InvocationStrategy {

  private String className;
  private GimpleFunction function;
  private final List<String> aliases = new ArrayList<>();
  private Map<GimpleParameter, ParamStrategy> params = Maps.newHashMap();
  private ReturnStrategy returnStrategy;
  
  private Labels labels = new Labels();
  private TypeOracle typeOracle;
  private ExprFactory exprFactory;
  private LocalStaticVarAllocator staticVarAllocator;
  private LocalVariableTable symbolTable;
  
  private Label beginLabel = new Label();
  private Label endLabel = new Label();
  
  private MethodGenerator mv;

  private boolean compilationFailed = false;

  public FunctionGenerator(String className, GimpleFunction function, TypeOracle typeOracle,
                           GlobalVarAllocator globalVarAllocator, UnitSymbolTable symbolTable) {
    this.className = className;
    this.function = function;
    this.typeOracle = typeOracle;
    this.params = this.typeOracle.forParameters(function.getParameters());
    this.returnStrategy = this.typeOracle.returnStrategyFor(function.getReturnType());
    this.staticVarAllocator = new LocalStaticVarAllocator("$" + function.getSafeMangledName() + "$", globalVarAllocator);
    this.symbolTable = new LocalVariableTable(symbolTable);
  }

  public String getMangledName() {
    return function.getMangledName();
  }

  public String getSafeMangledName() {
    return function.getSafeMangledName();
  }

  public List<String> getMangledNames() {
    List<String> names = Lists.newArrayList();
    names.add(function.getMangledName());
    names.addAll(aliases);
    return names;
  }

  public void addAlias(String alias) {
    aliases.add(alias);
  }

  public GimpleFunction getFunction() {
    return function;
  }

  public void emit(TreeLogger parentLogger, ClassVisitor cw) {

    try {
      TreeLogger logger = parentLogger.branch("Generating bytecode for " +
          function.getName() + " [" + function.getMangledName() + "]");
      logger.debug("Sources:", findSourceFiles());
      logger.debug("Aliases: " + aliases);
      logger.debug("Gimple:", function);

      logger.dump(function.getUnit().getSourceName(), function.getSafeMangledName(), "gimple", function);


      if (GimpleCompiler.TRACE) {
        System.out.println(function);
      }

      MethodNode methodNode = new MethodNode(ACC_PUBLIC | ACC_STATIC,
          function.getSafeMangledName(),
          getFunctionDescriptor(), null, null);

      mv = new MethodGenerator(methodNode);
      this.exprFactory = new ExprFactory(typeOracle, this.symbolTable, mv);

      mv.visitCode();
      mv.visitLabel(beginLabel);

      emitParamInitialization();
      scheduleLocalVariables();

      emitLocalVarInitialization();

      for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
        emitBasicBlock(basicBlock);
      }

      // Verify that GCC is not letting us fall through with out a return statement
      if (function.isEmpty() || function.getLastBasicBlock().fallsThrough()) {
        JExpr defaultReturnValue = returnStrategy.getDefaultReturnValue();
        defaultReturnValue.load(mv);
        mv.areturn(defaultReturnValue.getType());
      }

      mv.visitLabel(endLabel);

      // Javac does not like our variable table
      // https://bugs.openjdk.java.net/browse/JDK-8132697
      // Bug is somewhere in ClassReader.java
      // http://hg.openjdk.java.net/jdk8u/jdk8u/langtools/file/78f0aa619915/src/share/classes/com/sun/tools/javac/jvm/ClassReader.java

      mv.getLocalVarAllocator().emitDebugging(mv, beginLabel, endLabel);

      mv.visitMaxs(1, 1);
      mv.visitEnd();

      logger.dump(function.getUnit().getSourceName(), function.getSafeMangledName(), "j", toString(methodNode));

      // Reduce the size of the bytecode by applying simple optimizations
      PeepholeOptimizer.INSTANCE.optimize(methodNode);

      int estimatedSize = BytecodeSizeEstimator.estimateSize(methodNode);
      if (estimatedSize > 40_000) {
        System.err.println("WARNING: Method size of " + className + "." + function.getMangledName() + " may be exceeded.");
      }

      logger.dump(function.getUnit().getSourceName(), function.getSafeMangledName(), "opt.j", toString(methodNode));
      logger.dumpHtml(symbolTable, function, methodNode);

      try {
        methodNode.accept(cw);

      } catch (Exception e) {

        // Include the generated bytecode as part of the stack trace so we can
        // see what went wrong
        throw new InternalCompilerException("Error in generated byte code for " + function.getName() + "\n" +
            "Offending bytecode:\n" + toString(methodNode), e);
      }
    } catch (Exception e) {
      System.err.println("COMPILATION FAILED: " + getMangledName() + " in " +
          getCompilationUnit().getSourceName());

      parentLogger.dump(function.getUnit().getSourceName(), function.getSafeMangledName(), "error",
          Throwables.getStackTraceAsString(e));

      writeRuntimeStub(cw);

      compilationFailed = true;

      throw new InternalCompilerException(e);

    }
  }

  private void writeRuntimeStub(ClassVisitor cw) {

    MethodNode methodNode = new MethodNode(ACC_PUBLIC | ACC_STATIC,
        function.getSafeMangledName(),
        getFunctionDescriptor(), null, null);

    mv = new MethodGenerator(methodNode);
    this.exprFactory = new ExprFactory(typeOracle, this.symbolTable, mv);

    mv.visitCode();
    mv.anew(Type.getType(RuntimeException.class));
    mv.dup();
    mv.aconst("Compilation of " + getMangledName() + " in " + getCompilationUnit().getSourceName() +
        " failed at build time. Please review build logs for more details.");
    mv.invokeconstructor(Type.getType(RuntimeException.class), Type.getType(String.class));
    mv.athrow();
    mv.visitMaxs(1, 1);
    mv.visitEnd();

    methodNode.accept(cw);
  }

  private Object findSourceFiles() {
    Set<String> files = new HashSet<>();
    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(statement.getSourceFile() != null) {
          files.add(statement.getSourceFile());
        }
      }
    }
    return files;
  }

  private String toString(MethodNode methodNode) {
    try {
      Textifier p = new Textifier();
      methodNode.accept(new TraceMethodVisitor(p));
      StringWriter sw = new StringWriter();
      try (PrintWriter pw = new PrintWriter(sw)) {
        p.print(pw);
      }
      return sw.toString();
    } catch (Exception e) {
      return "<Exception generating bytecode: " + e.getClass().getName() + ": " + e.getMessage() + ">";
    }
  }

  private void emitParamInitialization() {
    // first we need to map the parameters to their indexes in the local variable table
    int numParameters = function.getParameters().size();
    List<List<JLValue>> paramIndexes = new ArrayList<>();

    for (int i = 0; i < numParameters; i++) {
      List<JLValue> paramVars = new ArrayList<>();
      GimpleParameter param = function.getParameters().get(i);
      ParamStrategy paramStrategy = params.get(param);
      List<Type> parameterTypes = paramStrategy.getParameterTypes();
      if(parameterTypes.size() == 1) {
        paramVars.add(mv.getLocalVarAllocator().reserve(param.getName(), parameterTypes.get(0)));
//        mv.visitParameter(param.getName(), 0);
      } else {
        for (int typeIndex = 0; typeIndex < parameterTypes.size(); typeIndex++) {
          String name = param.getName() + "$" + typeIndex;
          paramVars.add(mv.getLocalVarAllocator().reserve(name, parameterTypes.get(typeIndex)));
//          mv.visitParameter(name, 0);
        }
      }
      paramIndexes.add(paramVars);
    }

    // Now do any required initialization
    for (int i = 0; i < numParameters; i++) {
      GimpleParameter param = function.getParameters().get(i);
      ParamStrategy generator = params.get(param);
      GExpr expr = generator.emitInitialization(mv, param, paramIndexes.get(i), mv.getLocalVarAllocator());
      symbolTable.addVariable(param.getId(), expr);
    }
  }

  private void emitLocalVarInitialization() {
    
    mv.getLocalVarAllocator().initializeVariables(mv);
    
    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
      if(!decl.isStatic()) {
        GExpr lhs = symbolTable.getVariable(decl);
        if (decl.getValue() != null) {
          lhs.store(mv, exprFactory.findGenerator(decl.getValue()));
        }
      }
    }
  }

  public void emitLocalStaticVarInitialization(MethodGenerator mv) {

    if(compilationFailed) {
      return;
    }

    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
      if(decl.isStatic()) {
        GExpr lhs = symbolTable.getVariable(decl);
        if (decl.getValue() != null) {
          try {
            lhs.store(mv, exprFactory.findGenerator(decl.getValue()));
          } catch (Exception e) {
            throw new InternalCompilerException(String.format("static variable: %s in %s",
                decl.getName(),
                function.getMangledName()), e);
          }
        }
      }
    }
  }

  /*
   * Assign symbols to local variable slots.
   */
  private void scheduleLocalVariables() {

    // Dumb scheduling: give every local variable it's own slot
    for (GimpleVarDecl varDecl : function.getVariableDeclarations()) {
      try {
        GExpr generator;
        TypeStrategy factory = typeOracle.forType(varDecl.getType());
        generator = factory.variable(varDecl,
            varDecl.isStatic() ?
                staticVarAllocator :
                mv.getLocalVarAllocator());

        symbolTable.addVariable(varDecl.getId(), generator);
      } catch (Exception e) {
        throw new InternalCompilerException("Exception generating local variable " + varDecl, e);
      }
    }
  }

  private void emitBasicBlock(GimpleBasicBlock basicBlock) {
    mv.visitLabel(labels.of(basicBlock));

    Integer currentLineNumber = null;

    for (GimpleStatement ins : basicBlock.getStatements()) {
      Label insLabel = new Label();
      mv.visitLabel(insLabel);
      
      try {
        if (ins instanceof GimpleAssignment) {
          emitAssignment((GimpleAssignment) ins);
        } else if (ins instanceof GimpleReturn) {
          emitReturn((GimpleReturn) ins);
        } else if (ins instanceof GimpleGoto) {
          emitGoto((GimpleGoto) ins);
        } else if (ins instanceof GimpleConditional) {
          emitConditional((GimpleConditional) ins);
        } else if (ins instanceof GimpleCall) {
          emitCall((GimpleCall) ins);
        } else if (ins instanceof GimpleSwitch) {
          emitSwitch((GimpleSwitch) ins);
        } else {
          emitAsm(ins);
        }
      } catch (Exception e) {
        throw new InternalCompilerException("Exception compiling instruction " + ins, e);
      }
      
      if(ins.getLineNumber() != null && !Objects.equals(ins.getLineNumber(), currentLineNumber)) {
        mv.visitLineNumber(ins.getLineNumber(), insLabel);
        currentLineNumber = ins.getLineNumber();
      }
    }
  }

  private void emitAsm(GimpleStatement ins) {
    mv.invokestatic(Stdlib.class, "inlineAssembly", "()V");
  }

  private void emitSwitch(GimpleSwitch ins) {
    JExpr switchValue = exprFactory.findPrimitiveGenerator(ins.getValue());
    if(switchValue.getType() == Type.INT_TYPE) {
      switchValue.load(mv);
    } else if(switchValue.getType() == Type.LONG_TYPE) {
      switchValue.load(mv);
      mv.visitInsn(Opcodes.L2I);
    } else {
      throw new InternalCompilerException("Invalid type for switch: " + switchValue.getType());
    }

    Label defaultLabel = labels.of(ins.getDefaultCase().getBasicBlockIndex());

    int numCases = ins.getCaseCount();
    Label[] caseLabels = new Label[numCases];
    int[] caseValues = new int[numCases];

    int i = 0;
    for (GimpleSwitch.Case aCase : ins.getCases()) {
      for(int value = aCase.getLow(); value <= aCase.getHigh(); ++value) {
        caseLabels[i] = labels.of(aCase.getBasicBlockIndex());
        caseValues[i] = value;
        i++;
      }
    }
    mv.visitLookupSwitchInsn(defaultLabel, caseValues, caseLabels);
  }

  private void emitAssignment(GimpleAssignment ins) {

    if(isClobber(ins)) {
      return;
    }

    try {
      GExpr lhs = exprFactory.findGenerator(ins.getLHS());
      GExpr rhs = exprFactory.findGenerator(ins.getOperator(), ins.getOperands(), ins.getLHS().getType());
      
      lhs.store(mv, rhs);
      
    } catch (Exception e) {
      throw new RuntimeException("Exception compiling assignment to " + ins, e);
    }
  }

  private boolean isClobber(GimpleAssignment ins) {
    return ins.getOperator() == GimpleOp.CONSTRUCTOR &&
        ins.getOperands().get(0) instanceof GimpleConstructor &&
        ((GimpleConstructor) ins.getOperands().get(0)).isClobber();
  }

  private void emitGoto(GimpleGoto ins) {
    mv.visitJumpInsn(GOTO, labels.of(ins.getTarget()));
  }

  private void emitConditional(GimpleConditional ins) {
    ConditionGenerator generator = exprFactory.findConditionGenerator(ins.getOperator(), ins.getOperands());
        
    generator.emitJump(mv, labels.of(ins.getTrueLabel()), labels.of(ins.getFalseLabel()));
  }


  private void emitCall(GimpleCall ins) {
    CallGenerator callGenerator = exprFactory.findCallGenerator(ins.getFunction());
    callGenerator.emitCall(mv, exprFactory, ins);
  }

  private void emitReturn(GimpleReturn ins) {
    if(function.getReturnType() instanceof GimpleVoidType) {
      mv.areturn(Type.VOID_TYPE);
    } else {
      JExpr returnValue;
      if(ins.getValue() == null) {
        returnValue = returnStrategy.getDefaultReturnValue();
      } else {
        GExpr returnExpr = exprFactory.findGenerator(ins.getValue(), function.getReturnType());
        returnValue = returnStrategy.marshall(returnExpr);
      }
      returnValue.load(mv);
      mv.areturn(returnValue.getType());
    }
  }

  public String getFunctionDescriptor() {
    return TypeOracle.getMethodDescriptor(returnStrategy, getParamStrategies());
  }
  
  @Override
  public List<ParamStrategy> getParamStrategies() {
    List<ParamStrategy> parameterTypes = new ArrayList<>();
    for (GimpleParameter parameter : function.getParameters()) {
      ParamStrategy generator = params.get(parameter);
      parameterTypes.add(generator);
    }
    return parameterTypes;
  }

  @Override
  public boolean isVarArgs() {
    return false;
  }

  public Type returnType() {
    return returnStrategy.getType();
  }

  public ReturnStrategy getReturnStrategy() {
    return returnStrategy;
  }

  @Override
  public void invoke(MethodGenerator mv) {
    mv.invokestatic(getClassName(), function.getSafeMangledName(), getFunctionDescriptor(), false);
  }

  public GimpleCompilationUnit getCompilationUnit() {
    return function.getUnit();
  }

  @Override
  public Handle getMethodHandle() {
    return new Handle(H_INVOKESTATIC, className, function.getSafeMangledName(), getFunctionDescriptor());
  }

  public String getClassName() {
    return className;
  }

  @Override
  public String toString() {
    return className + "." + getMangledName() + "()";
  }

}
