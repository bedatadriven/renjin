package org.renjin.gcc.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionTable;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.call.StringLiteralGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.PrimitiveParamGenerator;
import org.renjin.gcc.codegen.param.WrappedPtrParamGenerator;
import org.renjin.gcc.codegen.ret.PrimitiveReturnGenerator;
import org.renjin.gcc.codegen.ret.PtrReturnGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.ret.VoidReturnGenerator;
import org.renjin.gcc.codegen.var.PrimitiveVarGenerator;
import org.renjin.gcc.codegen.var.PtrVarGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.codegen.var.VariableTable;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.ins.*;
import org.renjin.gcc.gimple.type.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Emits the bytecode for a Gimple function
 */
public class FunctionGenerator {

  private GimpleFunction function;
  private List<ParamGenerator> params = new ArrayList<ParamGenerator>();
  private ReturnGenerator returnGenerator;
  private LocalVarAllocator localVarAllocator;
  
  private Labels labels = new Labels();
  private VariableTable localVariables;
  private FunctionTable functionTable;
  
  private MethodVisitor mv;

  public FunctionGenerator(GimpleFunction function) {
    this.function = function;
    this.params = findParamGenerators(function.getParameters());
    this.returnGenerator = findReturnGenerator(function.getReturnType());
    this.localVarAllocator = new LocalVarAllocator(params);
  }
  
  public String getMangledName() {
    return function.getMangledName();
  }

  private List<ParamGenerator> findParamGenerators(List<GimpleParameter> parameters) {
    List<ParamGenerator> generators = new ArrayList<ParamGenerator>();
    int paramIndex = 0;
    for (GimpleParameter gimpleParameter : function.getParameters()) {
      ParamGenerator param = findParamGenerator(gimpleParameter, paramIndex);
      paramIndex += param.numSlots();
      generators.add(param);
    }
    return generators;
  }
  
  public void emit(ClassVisitor cw, VariableTable globalVars, FunctionTable functionTable) {
    this.localVariables = new VariableTable(globalVars);
    this.functionTable = functionTable;
    
    mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,
        function.getMangledName(), functionDescriptor(), null, null);
    mv.visitCode();
    
    emitParamInitialization();
    scheduleLocalVariables();
    
    emitLocalVarInitialization();

    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      emitBasicBlock(basicBlock);
    }
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }


  private void emitParamInitialization() {
    for (ParamGenerator param : params) {
      VarGenerator variable = param.emitInitialization(mv, localVarAllocator);
      localVariables.add(param.getGimpleId(), variable);
    }
  }


  private void emitLocalVarInitialization() {
    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
      if(decl.getValue() != null) {
        LValueGenerator lhs = (LValueGenerator) localVariables.get(decl);
        ExprGenerator rhs = findGenerator(decl.getValue());
        lhs.emitStore(mv, rhs);
      }
    }
  }

  private ParamGenerator findParamGenerator(GimpleParameter gimpleParameter, int index) {
    if(gimpleParameter.getType() instanceof GimplePrimitiveType) {
      return new PrimitiveParamGenerator(gimpleParameter, index);
      
    } else if(gimpleParameter.getType() instanceof GimpleIndirectType) {
      return new WrappedPtrParamGenerator(gimpleParameter, index);
    }
    
    throw new UnsupportedOperationException("Parameter: " + gimpleParameter);
  }

  /**
   * Assign symbols to local variable slots.
   */
  private void scheduleLocalVariables() {

    // Dumb scheduling: give every local variable it's own slot
    for (GimpleVarDecl varDecl : function.getVariableDeclarations()) {
      VarGenerator generator = findGeneratorForVariable(varDecl);
      localVariables.add(varDecl.getId(), generator);
    }
  }

  private VarGenerator findGeneratorForVariable(GimpleVarDecl varDecl) {
    if(varDecl.getType() instanceof GimplePrimitiveType) {
      GimplePrimitiveType primitiveType = (GimplePrimitiveType) varDecl.getType();
      int index = localVarAllocator.reserve(primitiveType.localVariableSlots());
      return new PrimitiveVarGenerator(index, varDecl.getName(), primitiveType);
    
    } else if(varDecl.getType() instanceof GimpleIndirectType) {
      GimplePointerType pointerType = (GimplePointerType) varDecl.getType();
      return new PtrVarGenerator(pointerType.getBaseType(), localVarAllocator);
      
    } else {
      throw new UnsupportedOperationException("var type: " + varDecl.getType());
    }
  }
  
  private ReturnGenerator findReturnGenerator(GimpleType returnType) {
    if(returnType instanceof GimpleVoidType) {
      return new VoidReturnGenerator();
      
    } else if(returnType instanceof GimplePrimitiveType) {
      return new PrimitiveReturnGenerator(returnType);
    
    } else if(returnType instanceof GimpleIndirectType) {
      return new PtrReturnGenerator(returnType);
    
    } else {
      throw new UnsupportedOperationException("Return type: " + returnType);
    }
  }

  private void emitBasicBlock(GimpleBasicBlock basicBlock) {
    mv.visitLabel(labels.of(basicBlock));

    for (GimpleIns ins : basicBlock.getInstructions()) {
      if(ins instanceof GimpleAssign) {
        emitAssignment((GimpleAssign)ins);
      } else if(ins instanceof GimpleReturn) {
        emitReturn((GimpleReturn) ins);
      } else if(ins instanceof GimpleGoto) {
        emitGoto((GimpleGoto) ins);
      } else if(ins instanceof GimpleConditional) {
        emitConditional((GimpleConditional) ins);
      } else if(ins instanceof GimpleCall) {
        emitCall((GimpleCall)ins);
      } else {
        throw new UnsupportedOperationException("ins: " + ins);
      }
    }
  }

  private void emitAssignment(GimpleAssign ins) {
    try {
      LValueGenerator lhs = (LValueGenerator) findGenerator(ins.getLHS());
      ExprGenerator rhs = findGenerator(lhs, ins.getOperator(), ins.getOperands());

      lhs.emitStore(mv, rhs);
    } catch (Exception e) {
      throw new RuntimeException("Exception translating assignment " + ins, e);
    }
  }

  private ExprGenerator findGenerator(LValueGenerator lhs, GimpleOp operator, List<GimpleExpr> operands) {
    if(operator == GimpleOp.CONVERT_EXPR) {
      PrimitiveGenerator primitiveGenerator = (PrimitiveGenerator) lhs;
      return new ConvertGenerator(findGenerator(operands.get(0)), primitiveGenerator.primitiveType());
    
    } else {
      return findGenerator(operator, operands);
    }
  }


  private void emitGoto(GimpleGoto ins) {
    mv.visitJumpInsn(GOTO, labels.of(ins.getTarget()));
  }

  private void emitConditional(GimpleConditional ins) {
    ConditionGenerator generator = (ConditionGenerator) findGenerator(ins.getOperator(), ins.getOperands());
        
    
    // jump if true
    generator.emitJump(mv, labels.of(ins.getTrueLabel()));
    
    // if false...
    mv.visitJumpInsn(GOTO, labels.of(ins.getFalseLabel()));
  }


  private void emitCall(GimpleCall ins) {

    if(MallocGenerator.isMalloc(ins.getFunction())) {
      emitMalloc(ins);
      
    } else {
      List<ExprGenerator> arguments = new ArrayList<ExprGenerator>();
      for (GimpleExpr argumentExpr : ins.getArguments()) {
        arguments.add(findGenerator(argumentExpr));
      }
      
      CallGenerator callGenerator = findCallGenerator(ins.getFunction());
      
      if(ins.getLhs() == null) {
        // call the function for its side effects
        callGenerator.emitCall(mv, arguments);
        
      } else {
        LValueGenerator lhs = (LValueGenerator) findGenerator(ins.getLhs());
        ExprGenerator callResult = callGenerator.expressionGenerator(arguments);
        
        lhs.emitStore(mv, callResult);
      }
    }
  }

  private void emitMalloc(GimpleCall ins) {
    LValueGenerator lhs = (LValueGenerator) findGenerator(ins.getLhs());
    ExprGenerator size = findGenerator(ins.getArguments().get(0));
    MallocGenerator mallocGenerator = new MallocGenerator(lhs, size);
    
    lhs.emitStore(mv, mallocGenerator);
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
      case MULT_EXPR:
      case EXACT_DIV_EXPR:
      case TRUNC_MOD_EXPR:
      case BIT_IOR_EXPR:
      case BIT_XOR_EXPR:
      case BIT_AND_EXPR:
        return new BinaryOpGenerator(op, 
            findGenerator(operands.get(0)), 
            findGenerator(operands.get(1)));
      
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
      
      case VAR_DECL:
      case NOP_EXPR:
      case MEM_REF:
      case INTEGER_CST:
      case REAL_CST:
      case ADDR_EXPR:
        return findGenerator(operands.get(0));

      case FIX_TRUNC_EXPR:
        return new TruncateExprGenerator(findGenerator(operands.get(0)));      
      
      case FLOAT_EXPR:
        return new DoubleGenerator(findGenerator(operands.get(0)));
      
      case NEGATE_EXPR:
        return new NegateGenerator(findGenerator(operands.get(0)));
      
      case TRUTH_NOT_EXPR:
        return new LogicalNotGenerator(findGenerator(operands.get(0)));
      
      case EQ_EXPR:
      case LT_EXPR:
      case LE_EXPR:
      case NE_EXPR:
      case GT_EXPR:
      case GE_EXPR:
        return new ComparisonGenerator(op,
            findGenerator(operands.get(0)),
            findGenerator(operands.get(1)));
      
      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }

  private ExprGenerator findGenerator(GimpleExpr expr) {
    if(expr instanceof SymbolRef) {
      return localVariables.get((SymbolRef) expr);
      
    } else if(expr instanceof GimpleConstant) {
      GimpleConstant constant = (GimpleConstant) expr;
      if (constant.isNull()) {
        return new NullPtrGenerator(constant.getType());
      } else {
        return new ConstValueGenerator(constant);
      }

    } else if(expr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) expr;
      if(addressOf.getValue() instanceof GimpleStringConstant) {
        return new StringLiteralGenerator(addressOf.getValue());        
      }
      throw new UnsupportedOperationException("address of " + addressOf.getValue() +
          " [" + addressOf.getValue().getClass().getSimpleName() + "]");
      
    } else if(expr instanceof GimpleMemRef) {
      return new MemRefGenerator(findGenerator(((GimpleMemRef) expr).getPointer()));
      
    } else {
      throw new UnsupportedOperationException(expr.getClass().getSimpleName());
    }
  }

  private CallGenerator findCallGenerator(GimpleExpr functionExpr) {
    if(functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if(addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return functionTable.find(ref);
      }
    }
    GimpleAddressOf address = (GimpleAddressOf) functionExpr;
    throw new UnsupportedOperationException("function: " + address.getValue() +
        " [" + address.getValue().getClass().getSimpleName() + "]");
  }
  
  
  private String functionDescriptor() {
    return Type.getMethodDescriptor(returnGenerator.type(), parameterTypes());
  }

  public Type[] parameterTypes() {
    List<Type> parameterTypes = new ArrayList<Type>();
    for (ParamGenerator param : params) {
      parameterTypes.addAll(param.getParameterTypes());
    }
    return parameterTypes.toArray(new Type[parameterTypes.size()]);
  }
  
  public Type returnType() {
    return returnGenerator.type();
  }
}
