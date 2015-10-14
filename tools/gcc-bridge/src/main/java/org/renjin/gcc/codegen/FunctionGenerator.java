package org.renjin.gcc.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
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
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleMemRef;
import org.renjin.gcc.gimple.expr.SymbolRef;
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
  private VariableTable localVariables = new VariableTable();
  
  private MethodVisitor mv;

  public FunctionGenerator(VariableTable parent, GimpleFunction function) {
    this.function = function;
    this.params = findParamGenerators(function.getParameters());
    this.returnGenerator = findReturnGenerator(function.getReturnType());
    this.localVarAllocator = new LocalVarAllocator(params);
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
  
  public void emit(ClassVisitor cw) {
    mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,
        function.getName(), functionDescriptor(), null, null);
    mv.visitCode();
    
    emitParamInitialization();
    
    scheduleLocalVariables();

    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      emitBasicBlock(basicBlock);
    }
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void emitParamInitialization() {
    int variableIndex = 0;
    for (ParamGenerator param : params) {
      VarGenerator variable = param.emitInitialization(mv, localVarAllocator);
      localVariables.add(param.getGimpleId(), variable);
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
        emitConditional((GimpleConditional)ins);
      } else {
        throw new UnsupportedOperationException("ins: " + ins);
      }
    }
  }



  private void emitAssignment(GimpleAssign ins) {
    try {
      LValueGenerator lhs = (LValueGenerator) findGenerator(ins.getLHS());
      ExprGenerator rhs = findGenerator(ins.getOperator(), ins.getOperands());

      lhs.emitStore(mv, rhs);
    } catch (Exception e) {
      throw new RuntimeException("Exception translating assignment " + ins, e);
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

  private void emitReturn(GimpleReturn ins) {
    ExprGenerator valueGenerator = findGenerator(ins.getValue());
    
    if(valueGenerator instanceof PrimitiveGenerator) {
      PrimitiveGenerator primitiveGenerator = (PrimitiveGenerator) valueGenerator;      
      primitiveGenerator.emitPush(mv);
      
      mv.visitInsn(primitiveGenerator.primitiveType().getOpcode(IRETURN));
    
    } else {
      throw new UnsupportedOperationException("Return: " + valueGenerator);
    }
  }

  private ExprGenerator findGenerator(GimpleOp op, List<GimpleExpr> operands) {
    switch (op) {
      case PLUS_EXPR:
      case MULT_EXPR:
      case EXACT_DIV_EXPR: 
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
        return findGenerator(operands.get(0));

      case FIX_TRUNC_EXPR:
        return new TruncateExprGenerator(findGenerator(operands.get(0)));      
      
      case NEGATE_EXPR:
        return new NegateGenerator(findGenerator(operands.get(0)));
      
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
      return new ConstGenerator((GimpleConstant) expr);

    } else if(expr instanceof GimpleMemRef) {
      return new MemRefGenerator(findGenerator(((GimpleMemRef) expr).getPointer()));
      
    } else {
      throw new UnsupportedOperationException(expr.getClass().getSimpleName());
    }
  }

  private String functionDescriptor() {
    List<Type> parameterTypes = new ArrayList<Type>();
    for (ParamGenerator param : params) {
      parameterTypes.addAll(param.getParameterTypes());
    }

    return Type.getMethodDescriptor(returnGenerator.type(), parameterTypes.toArray(new Type[parameterTypes.size()]));
  }
}
