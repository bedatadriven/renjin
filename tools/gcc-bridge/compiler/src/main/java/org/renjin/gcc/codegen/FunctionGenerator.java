package org.renjin.gcc.codegen;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.*;
import org.renjin.gcc.peephole.PeepholeOptimizer;
import org.renjin.gcc.symbols.LocalVariableTable;
import org.renjin.gcc.symbols.UnitSymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates the bytecode for a {@link GimpleFunction}
 */
public class FunctionGenerator {

  private String className;
  private GimpleFunction function;
  private Map<GimpleParameter, ParamStrategy> params = Maps.newHashMap();
  private ReturnStrategy returnStrategy;
  private LocalVarAllocator localVarAllocator;
  
  private Labels labels = new Labels();
  private TypeOracle typeOracle;
  private ExprFactory exprFactory;
  private LocalVariableTable symbolTable;
  
  private Label beginLabel = new Label();
  private Label endLabel = new Label();
  
  private MethodVisitor mv;

  public FunctionGenerator(String className, GimpleFunction function, TypeOracle typeOracle, UnitSymbolTable symbolTable) {
    this.className = className;
    this.function = function;
    this.typeOracle = typeOracle;
    this.params = this.typeOracle.forParameters(function.getParameters());
    this.returnStrategy = this.typeOracle.findReturnGenerator(function.getReturnType());
    this.symbolTable = new LocalVariableTable(symbolTable);
    this.localVarAllocator = new LocalVarAllocator();
    this.exprFactory = new ExprFactory(typeOracle, this.symbolTable, function.getCallingConvention());
  }

  public String getMangledName() {
    return function.getMangledName();
  }

  public GimpleFunction getFunction() {
    return function;
  }

  public void emit(ClassVisitor cw) {
    
    if(GimpleCompiler.TRACE) {
      System.out.println(function);
    }
    
    MethodNode methodNode = new MethodNode(ACC_PUBLIC | ACC_STATIC, 
        function.getMangledName(), 
        getFunctionDescriptor(), null, null);

    mv = methodNode;
    mv.visitCode();
    mv.visitLabel(beginLabel);
    
    emitParamInitialization();
    scheduleLocalVariables();
    
    emitLocalVarInitialization();

    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      emitBasicBlock(basicBlock);
    }
    mv.visitLabel(endLabel);

    // Javac does not like our variable table
    // https://bugs.openjdk.java.net/browse/JDK-8132697
    // Bug is somewhere in ClassReader.java
    // http://hg.openjdk.java.net/jdk8u/jdk8u/langtools/file/78f0aa619915/src/share/classes/com/sun/tools/javac/jvm/ClassReader.java

    // localVarAllocator.emitDebugging(mv, beginLabel, endLabel);

    mv.visitMaxs(1, 1);
    mv.visitEnd();

    // Reduce the size of the bytecode by applying simple optimizations
    PeepholeOptimizer.INSTANCE.optimize(methodNode);
    
    
    methodNode.accept(cw);


  }

  private void emitParamInitialization() {
    // first we need to map the parameters to their indexes in the local variable table
    int numParameters = function.getParameters().size();
    List<List<Var>> paramIndexes = new ArrayList<>();

    for (int i = 0; i < numParameters; i++) {
      List<Var> paramVars = new ArrayList<>();
      GimpleParameter param = function.getParameters().get(i);
      ParamStrategy paramStrategy = params.get(param);
      List<Type> parameterTypes = paramStrategy.getParameterTypes();
      if(parameterTypes.size() == 1) {
        paramVars.add(localVarAllocator.reserve(param.getName(), parameterTypes.get(0)));
      } else {
        for (int typeIndex = 0; typeIndex < parameterTypes.size(); typeIndex++) {
          paramVars.add(localVarAllocator.reserve(param.getName() + "$" + typeIndex, parameterTypes.get(typeIndex)));
        }
      }
      paramIndexes.add(paramVars);
    }

    // Now do any required initialization
    for (int i = 0; i < numParameters; i++) {
      GimpleParameter param = function.getParameters().get(i);
      ParamStrategy generator = params.get(param);
      ExprGenerator exprGenerator = generator.emitInitialization(mv, param, paramIndexes.get(i), localVarAllocator);
      symbolTable.addVariable(param.getId(), exprGenerator);
    }
  }

  private void emitLocalVarInitialization() {
    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
      VarGenerator lhs = (VarGenerator) symbolTable.getVariable(decl);
      Optional<ExprGenerator> initialValue;
      if(decl.getValue() == null) {
        initialValue = Optional.absent();
      } else {
        initialValue = Optional.of(exprFactory.findGenerator(decl.getValue()));
      }
      
      if(GimpleCompiler.TRACE) {
        System.out.println(getCompilationUnit().getName() + ": " + decl + " = " + initialValue +
            " [" + lhs.getClass().getName() + "]");
      }
      
      lhs.emitDefaultInit(mv, initialValue);
    }
  }

  /*
   * Assign symbols to local variable slots.
   */
  private void scheduleLocalVariables() {

    // Dumb scheduling: give every local variable it's own slot
    for (GimpleVarDecl varDecl : function.getVariableDeclarations()) {
      
      try {
        VarGenerator generator;
        TypeStrategy factory = typeOracle.forType(varDecl.getType());
        generator = factory.varGenerator(varDecl, localVarAllocator);

        symbolTable.addVariable(varDecl.getId(), generator);
      } catch (Exception e) {
        throw new InternalCompilerException("Exception generating local variable " + varDecl, e);
      }
    }
  }

  private void emitBasicBlock(GimpleBasicBlock basicBlock) {
    mv.visitLabel(labels.of(basicBlock));

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
          throw new UnsupportedOperationException("ins: " + ins);
        }
      } catch (Exception e) {
        throw new InternalCompilerException("Exception compiling instruction " + ins, e);
      }
      
      if(ins.getLineNumber() != null) {
        mv.visitLineNumber(ins.getLineNumber(), insLabel);
      }
    }
  }

  private void emitSwitch(GimpleSwitch ins) {
    ExprGenerator valueGenerator = exprFactory.findGenerator(ins.getValue());
    valueGenerator.emitPrimitiveValue(mv);
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
    try {
      ExprGenerator lhs = exprFactory.findGenerator(ins.getLHS());
      ExprGenerator rhs = exprFactory.findGenerator(ins.getOperator(), ins.getOperands(), lhs.getGimpleType());
      lhs.emitStore(mv, rhs);
    } catch (Exception e) {
      throw new RuntimeException("Exception compiling assignment " + ins, e);
    }
  }

  private void emitGoto(GimpleGoto ins) {
    mv.visitJumpInsn(GOTO, labels.of(ins.getTarget()));
  }

  private void emitConditional(GimpleConditional ins) {
    ConditionGenerator generator = exprFactory.findConditionGenerator(ins.getOperator(), ins.getOperands());
        
    generator.emitJump(mv, labels.of(ins.getTrueLabel()), labels.of(ins.getFalseLabel()));
  }


  private void emitCall(GimpleCall ins) {

    if(MallocGenerator.isMalloc(ins.getFunction())) {
      emitMalloc(ins);
      
    } else {
      List<ExprGenerator> arguments = new ArrayList<ExprGenerator>();
      for (GimpleExpr argumentExpr : ins.getOperands()) {
        arguments.add(exprFactory.findGenerator(argumentExpr));
      }
      
      CallGenerator callGenerator = exprFactory.findCallGenerator(ins.getFunction());
      
      if(ins.getLhs() == null) {
        // call the function for its side effects
        callGenerator.emitCallAndPopResult(mv, arguments);
        
      } else {
        ExprGenerator lhs = exprFactory.findGenerator(ins.getLhs());
        ExprGenerator callResult =  callGenerator.expressionGenerator(ins.getLhs().getType(), arguments);
        
        lhs.emitStore(mv, exprFactory.maybeCast(callResult, lhs.getGimpleType()));
      }
    }
  }


  private void emitMalloc(GimpleCall ins) {
    ExprGenerator lhs = exprFactory.findGenerator(ins.getLhs());
    ExprGenerator size = exprFactory.findGenerator(ins.getOperands().get(0));
    
    lhs.emitStore(mv, typeOracle.forType(lhs.getGimpleType()).mallocExpression(size));
  }


  private void emitReturn(GimpleReturn ins) {
    if(ins.getValue() == null) {
      returnStrategy.emitReturnDefault(mv);
      
    } else {
      returnStrategy.emitReturnValue(mv, exprFactory.findGenerator(ins.getValue(), function.getReturnType()));
    }
  }

  public String getFunctionDescriptor() {
    return Type.getMethodDescriptor(returnStrategy.getType(), parameterTypes());
  }

  public List<ParamStrategy> getParamGenerators() {
    List<ParamStrategy> parameterTypes = new ArrayList<ParamStrategy>();
    for (GimpleParameter parameter : function.getParameters()) {
      ParamStrategy generator = params.get(parameter);
      parameterTypes.add(generator);
    }
    return parameterTypes;
  }
  
  public Type[] parameterTypes() {
    List<Type> types = new ArrayList<Type>();
    for (ParamStrategy generator : getParamGenerators()) {
      types.addAll(generator.getParameterTypes());
    }
    return types.toArray(new Type[types.size()]);
  }
  
  public Type returnType() {
    return returnStrategy.getType();
  }

  public ReturnStrategy getReturnStrategy() {
    return returnStrategy;
  }

  public GimpleCompilationUnit getCompilationUnit() {
    return function.getUnit();
  }


  public Handle getMethodHandle() {
    return new Handle(H_INVOKESTATIC, className, function.getMangledName(), getFunctionDescriptor());
  }


  public String getClassName() {
    return className;
  }
}
