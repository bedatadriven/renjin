package org.renjin.gcc.codegen;

import com.google.common.collect.Maps;
import org.objectweb.asm.*;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.CastGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.type.TypeFactory;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.ins.*;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.symbols.LocalVariableTable;
import org.renjin.gcc.symbols.UnitSymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * Emits the bytecode for a Gimple function
 */
public class FunctionGenerator {

  private String className;
  private GimpleFunction function;
  private Map<GimpleParameter, ParamGenerator> params = Maps.newHashMap();
  private ReturnGenerator returnGenerator;
  private LocalVarAllocator localVarAllocator;
  
  private Labels labels = new Labels();
  private GeneratorFactory generatorFactory;
  private ExprFactory exprFactory;
  private LocalVariableTable symbolTable;
  
  private Label beginLabel = new Label();
  private Label endLabel = new Label();
  
  private MethodVisitor mv;

  public FunctionGenerator(String className, GimpleFunction function, GeneratorFactory generatorFactory, UnitSymbolTable symbolTable) {
    this.className = className;
    this.function = function;
    this.generatorFactory = generatorFactory;
    this.params = this.generatorFactory.forParameters(function.getParameters());
    this.returnGenerator = this.generatorFactory.findReturnGenerator(function.getReturnType());
    this.symbolTable = new LocalVariableTable(symbolTable);
    this.localVarAllocator = new LocalVarAllocator();
    this.exprFactory = new ExprFactory(generatorFactory, this.symbolTable, function.getCallingConvention());
  }

  public String getMangledName() {
    return function.getMangledName();
  }

  public GimpleFunction getFunction() {
    return function;
  }

  public void emit(ClassVisitor cw) {
    System.out.println(function);
    
    mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,
        function.getMangledName(), getFunctionDescriptor(), null, null);
    mv.visitCode();
    mv.visitLabel(beginLabel);
    
    emitParamInitialization();
    scheduleLocalVariables();
    
    emitLocalVarInitialization();

    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      emitBasicBlock(basicBlock);
    }
    mv.visitLabel(endLabel);
    emitVariableDebugging();
    
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void emitParamInitialization() {
    // first we need to map the parameters to their indexes in the local variable table
    int numParameters = function.getParameters().size();
    int[] paramIndexes = new int[numParameters];

    for (int i = 0; i < numParameters; i++) {
      ParamGenerator generator = params.get(function.getParameters().get(i));
      paramIndexes[i] = localVarAllocator.reserve(generator.numSlots());
    }

    // Now do any required initialization
    for (int i = 0; i < numParameters; i++) {
      GimpleParameter param = function.getParameters().get(i);
      ParamGenerator generator = params.get(param);
      ExprGenerator exprGenerator = generator.emitInitialization(mv, paramIndexes[i], localVarAllocator);
      symbolTable.addVariable(param.getId(), exprGenerator);
    }
  }

  private void emitLocalVarInitialization() {
    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
      VarGenerator lhs = (VarGenerator) symbolTable.getVariable(decl);
      if(decl.getValue() == null) {
        lhs.emitDefaultInit(mv);

      } else {
        ExprGenerator rhs = exprFactory.findGenerator(decl.getValue());
        lhs.emitStore(mv, rhs);
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
        VarGenerator generator;
        TypeFactory factory = generatorFactory.forType(varDecl.getType());
        if (varDecl.isAddressable()) {
          generator = factory.addressableVarGenerator(localVarAllocator);
        } else {
          generator = factory.varGenerator(localVarAllocator);
        }

        symbolTable.addVariable(varDecl.getId(), generator);
      } catch (Exception e) {
        throw new InternalCompilerException("Exception generating local variable " + varDecl, e);
      }
    }
  }

  private void emitBasicBlock(GimpleBasicBlock basicBlock) {
    mv.visitLabel(labels.of(basicBlock));

    for (GimpleIns ins : basicBlock.getInstructions()) {
      Label insLabel = new Label();
      mv.visitLabel(insLabel);
      
      try {
        if (ins instanceof GimpleAssign) {
          emitAssignment((GimpleAssign) ins);
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

    int numCases = ins.getCases().size();
    Label[] caseLabels = new Label[numCases];
    int[] caseValues = new int[numCases];

    for(int i=0;i < numCases; i++){
      GimpleSwitch.Case aCase = ins.getCases().get(i);
      caseLabels[i] = labels.of(aCase.getBasicBlockIndex());
      caseValues[i] = aCase.getLow();
      if(aCase.getLow() != aCase.getHigh()) {
        throw new UnsupportedOperationException("Tablelookup not yet supported.\n");
      }
    }
    mv.visitLookupSwitchInsn(defaultLabel, caseValues, caseLabels);
    
  }

  private void emitAssignment(GimpleAssign ins) {
    try {
      ExprGenerator lhs = exprFactory.findGenerator(ins.getLHS());
      ExprGenerator rhs = exprFactory.findGenerator(lhs, ins.getOperator(), ins.getOperands());

      lhs.emitStore(mv, maybeCast(rhs, lhs.getGimpleType()));
    } catch (Exception e) {
      throw new RuntimeException("Exception compiling assignment " + ins, e);
    }
  }

  private ExprGenerator maybeCast(ExprGenerator rhs, GimpleType lhsType) {
    if(lhsType instanceof GimplePrimitiveType) {

      if (rhs.getGimpleType() instanceof GimplePrimitiveType) {
        if (!lhsType.equals(rhs.getGimpleType())) {
          return new CastGenerator(rhs, (GimplePrimitiveType) lhsType);
        }
      }
    }
    return rhs;
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

    } else if(MallocGenerator.isFree(ins.getFunction())) {
      // NO OP
      // We have a garbage collector, muwahaha :-)
      
    } else {
      List<ExprGenerator> arguments = new ArrayList<ExprGenerator>();
      for (GimpleExpr argumentExpr : ins.getArguments()) {
        arguments.add(exprFactory.findGenerator(argumentExpr));
      }
      
      CallGenerator callGenerator = exprFactory.findCallGenerator(ins.getFunction());
      
      if(ins.getLhs() == null) {
        // call the function for its side effects
        callGenerator.emitCall(mv, arguments);
        discardReturnValue(mv, callGenerator.returnType());
        
      } else {
        ExprGenerator lhs = exprFactory.findGenerator(ins.getLhs());
        ExprGenerator callResult = callGenerator.expressionGenerator(arguments);
        
        lhs.emitStore(mv, callResult);
      }
    }
  }

  private void discardReturnValue(MethodVisitor mv, Type type) {
    if(!type.equals(Type.VOID_TYPE)) {
      int stackSize = type.getSize();
      if(stackSize == 1) {
        mv.visitInsn(Opcodes.POP);
      } else if(stackSize == 2) {
        mv.visitInsn(Opcodes.POP2);
      } else {
        throw new InternalCompilerException("Unexpected size: " + stackSize);
      }
    }
  }

  private void emitMalloc(GimpleCall ins) {
    ExprGenerator lhs = exprFactory.findGenerator(ins.getLhs());
    ExprGenerator size = exprFactory.findGenerator(ins.getArguments().get(0));
    
    lhs.emitStore(mv, generatorFactory.forType(lhs.getGimpleType()).mallocExpression(size) );
  }

  private void emitReturn(GimpleReturn ins) {
    if(ins.getValue() == null) {
      returnGenerator.emitVoidReturn(mv);
      
    } else {
      returnGenerator.emitReturn(mv, exprFactory.findGenerator(ins.getValue()));
    }
  }

  public String getFunctionDescriptor() {
    return Type.getMethodDescriptor(returnGenerator.getType(), parameterTypes());
  }

  public List<ParamGenerator> getParamGenerators() {
    List<ParamGenerator> parameterTypes = new ArrayList<ParamGenerator>();
    for (GimpleParameter parameter : function.getParameters()) {
      ParamGenerator generator = params.get(parameter);
      parameterTypes.add(generator);
    }
    return parameterTypes;
  }
  
  public Type[] parameterTypes() {
    return ParamGenerator.parameterTypes(getParamGenerators());
  }
  
  public Type returnType() {
    return returnGenerator.getType();
  }

  public ReturnGenerator getReturnGenerator() {
    return returnGenerator;
  }

  public GimpleCompilationUnit getCompilationUnit() {
    return function.getUnit();
  }

  private void emitVariableDebugging() {
    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
      if(decl.isNamed()) {
        ExprGenerator generator = symbolTable.getVariable(decl);
        if (generator instanceof VarGenerator) {
          ((VarGenerator) generator).emitDebugging(mv, toJavaSafeName(decl.getName()), beginLabel, endLabel);
        }
      }
    }
  }

  private String toJavaSafeName(String name) {
    return name.replace('.', '$');
  }

  public Handle getMethodHandle() {
    return new Handle(H_INVOKESTATIC, className, function.getMangledName(), getFunctionDescriptor());
  }


  public String getClassName() {
    return className;
  }
}
