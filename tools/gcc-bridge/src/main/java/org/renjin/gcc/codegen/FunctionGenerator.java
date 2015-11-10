package org.renjin.gcc.codegen;

import com.google.common.collect.Maps;
import org.objectweb.asm.*;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunPtrCallGenerator;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.condition.ComplexCmpGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.PointerCmpGenerator;
import org.renjin.gcc.codegen.condition.PrimitiveCmpGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.type.TypeFactory;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.ins.*;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
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
    //emitVariableDebugging();
    
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
      VarGenerator lhs = (VarGenerator) symbolTable.get(decl);
      if(decl.getValue() == null || decl.getValue() instanceof GimpleConstructor) {
        lhs.emitDefaultInit(mv);

      } else {
        ExprGenerator rhs = findGenerator(decl.getValue());
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
    ExprGenerator valueGenerator = findGenerator(ins.getValue());
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
      ExprGenerator lhs = findGenerator(ins.getLHS());
      ExprGenerator rhs = findGenerator(lhs, ins.getOperator(), ins.getOperands());

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

  private ExprGenerator findGenerator(ExprGenerator lhs, GimpleOp operator, List<GimpleExpr> operands) {
    if(operator == GimpleOp.CONVERT_EXPR ||
       operator == GimpleOp.FLOAT_EXPR ||
       operator == GimpleOp.FIX_TRUNC_EXPR) {
      
      return new CastGenerator(findGenerator(operands.get(0)), (GimplePrimitiveType) lhs.getGimpleType());
    
    } 
    return findGenerator(operator, operands);
  }

  private void emitGoto(GimpleGoto ins) {
    mv.visitJumpInsn(GOTO, labels.of(ins.getTarget()));
  }

  private void emitConditional(GimpleConditional ins) {
    ConditionGenerator generator = findConditionGenerator(ins.getOperator(), ins.getOperands());
        
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
        arguments.add(findGenerator(argumentExpr));
      }
      
      CallGenerator callGenerator = findCallGenerator(ins.getFunction());
      
      if(ins.getLhs() == null) {
        // call the function for its side effects
        callGenerator.emitCall(mv, arguments);
        discardReturnValue(mv, callGenerator.returnType());
        
      } else {
        ExprGenerator lhs = findGenerator(ins.getLhs());
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
    ExprGenerator lhs = findGenerator(ins.getLhs());
    ExprGenerator size = findGenerator(ins.getArguments().get(0));
    
    lhs.emitStore(mv, generatorFactory.forType(lhs.getGimpleType()).mallocExpression(size) );
  }

  private void emitReturn(GimpleReturn ins) {
    if(ins.getValue() == null) {
      returnGenerator.emitVoidReturn(mv);
      
    } else {
      returnGenerator.emitReturn(mv, findGenerator(ins.getValue()));
    }
  }

  private ExprGenerator findGenerator(GimpleOp op, List<GimpleExpr> operands) {
    switch (op) {
      case PLUS_EXPR:
      case MINUS_EXPR:
      case MULT_EXPR:
      case RDIV_EXPR:
      case TRUNC_DIV_EXPR:
      case EXACT_DIV_EXPR:
      case TRUNC_MOD_EXPR:
      case BIT_IOR_EXPR:
      case BIT_XOR_EXPR:
      case BIT_AND_EXPR:
        return findBinOpGenerator(op, operands);

      case POINTER_PLUS_EXPR:
        return new PtrPlusGenerator(
            findGenerator(operands.get(0)),
            findGenerator(operands.get(1)));
      
      case BIT_NOT_EXPR:
        return new BitwiseNotGenerator(findGenerator(operands.get(0)));

      case LSHIFT_EXPR:
      case RSHIFT_EXPR:
        return new BitwiseShiftGenerator(
            op,
            findGenerator(operands.get(0)), 
            findGenerator(operands.get(1)));
      
      case PAREN_EXPR:
      case VAR_DECL:
      case PARM_DECL:
      case NOP_EXPR:
      case MEM_REF:
      case INTEGER_CST:
      case REAL_CST:
      case COMPLEX_CST:
      case ADDR_EXPR:
      case ARRAY_REF:
      case COMPONENT_REF:
      case REALPART_EXPR:
      case IMAGPART_EXPR:
        return findGenerator(operands.get(0));
      
      case COMPLEX_EXPR:
        return new ComplexGenerator(findGenerator(operands.get(0)));
      
      case NEGATE_EXPR:
        return new NegateGenerator(findGenerator(operands.get(0)));
      
      case TRUTH_NOT_EXPR:
        return new LogicalNotGenerator(findGenerator(operands.get(0)));
      
      case TRUTH_AND_EXPR:
        return new LogicalAndGenerator(
            findGenerator(operands.get(0)),
            findGenerator(operands.get(1)));
      
      case TRUTH_OR_EXPR:
        return new LogicalOrGenerator(
            findGenerator(operands.get(0)),
            findGenerator(operands.get(1))); 
      
      case EQ_EXPR:
      case LT_EXPR:
      case LE_EXPR:
      case NE_EXPR:
      case GT_EXPR:
      case GE_EXPR:
        return new ConditionExprGenerator(
          findComparisonGenerator(op,
              findGenerator(operands.get(0)),
              findGenerator(operands.get(1))));
      
      case MAX_EXPR:
        return new MaxGenerator(
            findGenerator(operands.get(0)), 
            findGenerator(operands.get(1)));

      case ABS_EXPR:
        return new AbsGenerator(
            findGenerator(operands.get(0)));
      
      case UNORDERED_EXPR:
        return new UnorderedExprGenerator(
            findGenerator(operands.get(0)),
            findGenerator(operands.get(1)));
      
      case CONJ_EXPR:
        return new ConjugateGenerator(
            findGenerator(operands.get(0)));
      
      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }

  private ExprGenerator findBinOpGenerator(GimpleOp op, List<GimpleExpr> operands) {
    ExprGenerator x = findGenerator(operands.get(0));
    ExprGenerator y = findGenerator(operands.get(1));
    
    if(x.getGimpleType() instanceof GimpleComplexType &&
       y.getGimpleType() instanceof GimpleComplexType) {
      
      return new ComplexBinOperator(op, x, y);
   
    } else if(x.getGimpleType() instanceof GimplePrimitiveType &&
              y.getGimpleType() instanceof GimplePrimitiveType) {

      return new PrimitiveBinOpGenerator(op, x, y);
      
    } 
      
    throw new UnsupportedOperationException(op.name() + ": " + x.getGimpleType() + ", " + y.getGimpleType());
  }
  
  private ConditionGenerator findConditionGenerator(GimpleOp op, List<GimpleExpr> operands) {
    if(operands.size() == 2) {
      return findComparisonGenerator(op, 
          findGenerator(operands.get(0)), 
          findGenerator(operands.get(1)));
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private ConditionGenerator findComparisonGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {

    if(x.getGimpleType() instanceof org.renjin.gcc.gimple.type.GimpleComplexType) {
      return new ComplexCmpGenerator(op, x, y);
      
    } else if(x.getGimpleType() instanceof GimplePrimitiveType) {
      return new PrimitiveCmpGenerator(op, x, y);

    } else if(x.getGimpleType() instanceof GimpleIndirectType) {
      return new PointerCmpGenerator(op, x, y);
      
    } else {
      throw new UnsupportedOperationException("Unsupported comparison " + op + " between types " + 
          x.getGimpleType() + " and " + y.getGimpleType());
    }
  }

  private ExprGenerator findGenerator(GimpleExpr expr) {
    if(expr instanceof SymbolRef) {
      return symbolTable.get((SymbolRef) expr);
      
    } else if(expr instanceof GimpleConstant) {
      GimpleConstant constant = (GimpleConstant) expr;
      if (constant.isNull()) {
        return new NullPtrGenerator(constant.getType());
      } else if (constant instanceof GimplePrimitiveConstant) {
        return new PrimitiveConstValueGenerator((GimplePrimitiveConstant) constant);
      } else if (constant instanceof GimpleComplexConstant) {
        return new ComplexConstGenerator((GimpleComplexConstant) constant);
      } else if (constant instanceof GimpleStringConstant) {
        return new StringConstantGenerator(constant);
      }
      
    } else if(expr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) expr;
      if(addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef functionRef = (GimpleFunctionRef) addressOf.getValue();
        return new FunctionRefGenerator(symbolTable.findHandle(functionRef, function.getCallingConvention()));
      
      } else {
        ExprGenerator value = findGenerator(addressOf.getValue());
        return value.addressOf();
      }

    } else if(expr instanceof GimpleMemRef) {
      return findGenerator(((GimpleMemRef) expr).getPointer()).valueOf();

    } else if(expr instanceof GimpleArrayRef) {
      GimpleArrayRef arrayRef = (GimpleArrayRef) expr;
      ExprGenerator arrayGenerator = findGenerator(arrayRef.getArray());
      ExprGenerator indexGenerator = findGenerator(arrayRef.getIndex());
      return arrayGenerator.elementAt(indexGenerator);

    } else if(expr instanceof GimpleConstantRef) {
      GimpleConstant constant = ((GimpleConstantRef) expr).getValue();
      return findGenerator(constant);

    } else if(expr instanceof GimpleComplexPartExpr) {
      GimpleExpr complexExpr = ((GimpleComplexPartExpr) expr).getComplexValue();
      ExprGenerator complexGenerator = findGenerator(complexExpr);
      if(expr instanceof GimpleRealPartExpr) {
        return complexGenerator.realPart();
      } else {
        return complexGenerator.imaginaryPart();
      }
    } else if (expr instanceof GimpleComponentRef) {
      GimpleComponentRef componentRef = (GimpleComponentRef) expr;
      GimpleExpr valueExpr = componentRef.getValue();
      ExprGenerator valueExprGenerator = findGenerator(valueExpr);
      return valueExprGenerator.memberOf(componentRef.memberName());
    }

    throw new UnsupportedOperationException(expr + " [" + expr.getClass().getSimpleName() + "]");
  }

  private CallGenerator findCallGenerator(GimpleExpr functionExpr) {
    if(functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return symbolTable.findCallGenerator(ref, function.getCallingConvention());
      }
      GimpleAddressOf address = (GimpleAddressOf) functionExpr;
      throw new UnsupportedOperationException("function ref: " + address.getValue() +
          " [" + address.getValue().getClass().getSimpleName() + "]");
      
    } else if(functionExpr instanceof SymbolRef) {
      ExprGenerator exprGenerator = findGenerator(functionExpr);
      return new FunPtrCallGenerator(generatorFactory, exprGenerator);
    }
    throw new UnsupportedOperationException("function: " + functionExpr);
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
        ExprGenerator generator = symbolTable.get(decl);
        if (generator instanceof VarGenerator) {
          ((VarGenerator) generator).emitDebugging(mv, decl.getName(), beginLabel, endLabel);
        }
      }
    }
  }

  public Handle getMethodHandle() {
    return new Handle(H_INVOKESTATIC, className, function.getMangledName(), getFunctionDescriptor());
  }


  public String getClassName() {
    return className;
  }
}
