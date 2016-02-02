package org.renjin.gcc.codegen.expr;

import com.google.common.base.Optional;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunPtrCallGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.PointerCmpGenerator;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.complex.*;
import org.renjin.gcc.codegen.type.fun.FunctionRefGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveCmpGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
import org.renjin.gcc.codegen.type.primitive.StringConstantGenerator;
import org.renjin.gcc.codegen.type.primitive.op.*;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrCmpGenerator;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.symbols.SymbolTable;

import java.util.ArrayList;
import java.util.List;


/**
 * Creates {@code ExprGenerator}s from {@code GimpleExpr}s
 */
public class ExprFactory {
  private final TypeOracle typeOracle;
  private final SymbolTable symbolTable;
  private final CallingConvention callingConvention;

  public ExprFactory(TypeOracle typeOracle, SymbolTable symbolTable, CallingConvention callingConvention) {
    this.typeOracle = typeOracle;
    this.symbolTable = symbolTable;
    this.callingConvention = callingConvention;
  }


  public ExprGenerator findGenerator(GimpleOp operator, List<GimpleExpr> operands, GimpleType expectedType) {
    return maybeCast(findGenerator(operator, operands), expectedType);
  }

  public ExprGenerator findGenerator(GimpleExpr expr, GimpleType expectedType) {
    return maybeCast(findGenerator(expr), expectedType);
  }

  public ExprGenerator maybeCast(ExprGenerator rhs, GimpleType lhsType) {
    if(lhsType instanceof GimplePrimitiveType) {

      if (rhs.getGimpleType() instanceof GimplePrimitiveType) {
        if (!lhsType.equals(rhs.getGimpleType())) {
          return new CastGenerator(rhs, (GimplePrimitiveType) lhsType);
        }
      }
    } else if(
        lhsType.isPointerTo(GimpleRecordType.class) &&
            rhs.getGimpleType().isPointerTo(GimpleVoidType.class)) {

      GimpleRecordType recordType = lhsType.getBaseType();
      return ((RecordClassTypeStrategy) typeOracle.forType(recordType)).voidCast(rhs);
    }
    return rhs;
  }

  public ExprGenerator findGenerator(GimpleExpr expr) {
    if(expr instanceof GimpleSymbolRef) {
      ExprGenerator variable = symbolTable.getVariable((GimpleSymbolRef) expr);
      if(variable == null) {
        throw new InternalCompilerException("No such variable: " + expr);
      }
      return variable;

    } else if(expr instanceof GimpleConstant) {
      return forConstant((GimpleConstant) expr);

    } else if(expr instanceof GimpleConstructor) {
      return forConstructor((GimpleConstructor) expr);

    } else if(expr instanceof GimpleNopExpr) {
      return findGenerator(((GimpleNopExpr) expr).getValue(), expr.getType());

    } else if(expr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) expr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef functionRef = (GimpleFunctionRef) addressOf.getValue();
        return new FunctionRefGenerator(symbolTable.findHandle(functionRef, callingConvention));

      } else {
        ExprGenerator value = findGenerator(addressOf.getValue());
        return value.addressOf();
      }

    } else if(expr instanceof GimpleOpExpr) {
      // This is an artificial node we introduce during analysis to produce
      // code better suited to a stack-based interpreter
      GimpleOpExpr opExpr = (GimpleOpExpr) expr;
      return findGenerator(opExpr.getOp(), opExpr.getOperands(), opExpr.getType());

    } else if(expr instanceof GimpleCallExpr) {
      // Another artificial node for nested calls
      GimpleCallExpr callExpr = (GimpleCallExpr) expr;
      return findCallExpression(callExpr.getType(), callExpr.getFunction(), callExpr.getArguments());

    } else if(expr instanceof GimpleMemRef) {
      ExprGenerator pointerExpr = findGenerator(((GimpleMemRef) expr).getPointer());
      ExprGenerator offsetExpr = findGenerator(((GimpleMemRef) expr).getOffset());
      if(offsetExpr.isConstantIntEqualTo(0) || offsetExpr instanceof NullPtrGenerator) {
        return pointerExpr.valueOf();
      } else {
        return pointerExpr.pointerPlus(offsetExpr).valueOf();
      }
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
      if (expr instanceof GimpleRealPartExpr) {
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

  private ExprGenerator findCallExpression(GimpleType returnType, GimpleExpr function, List<GimpleExpr> argumentExprs) {
    List<ExprGenerator> arguments = new ArrayList<>();
    for (GimpleExpr argumentExpr : argumentExprs) {
      arguments.add(findGenerator(argumentExpr));
    }

    CallGenerator callGenerator = findCallGenerator(function);
    return maybeCast(callGenerator.expressionGenerator(returnType, arguments), returnType);
  }

  private ExprGenerator forConstructor(GimpleConstructor expr) {
    return typeOracle.forType(expr.getType()).constructorExpr(this, expr);
  }

  public CallGenerator findCallGenerator(GimpleExpr functionExpr) {
    if(functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return symbolTable.findCallGenerator(ref, callingConvention);
      }
      GimpleAddressOf address = (GimpleAddressOf) functionExpr;
      throw new UnsupportedOperationException("function ref: " + address.getValue() +
          " [" + address.getValue().getClass().getSimpleName() + "]");

    } else if(functionExpr instanceof GimpleOpExpr) {
      GimpleOp op = ((GimpleOpExpr) functionExpr).getOp();
      if(op == GimpleOp.VAR_DECL || op == GimpleOp.NOP_EXPR) {
        return findCallGenerator(((GimpleOpExpr) functionExpr).getOperands().get(0));
      }
    }

    // Assume this is a funciton ptr expression  
    ExprGenerator exprGenerator = findGenerator(functionExpr);
    return new FunPtrCallGenerator(typeOracle, exprGenerator);
  }

  public ConditionGenerator findConditionGenerator(GimpleOp op, List<GimpleExpr> operands) {
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
      if(x instanceof RecordUnitPtrGenerator && y instanceof RecordUnitPtrGenerator) {
        return new RecordUnitPtrCmpGenerator(op, x, y);
      } else {
        return new PointerCmpGenerator(op, x, y);
      }

    } else {
      throw new UnsupportedOperationException("Unsupported comparison " + op + " between types " +
          x.getGimpleType() + " and " + y.getGimpleType());
    }
  }
  
  private Value forPrimitive(GimpleExpr operand) {
    
  }
  
  private Value forPrimitive(GimpleOp op, List<GimpleExpr> operands) {
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
        return new PrimitiveBinOpGenerator(op, 
            forPrimitive(operands.get(0)),
            forPrimitive(operands.get(1)));

      case BIT_NOT_EXPR:
        return new BitwiseNotGenerator(forPrimitive(operands.get(0)));

      case LSHIFT_EXPR:
      case RSHIFT_EXPR:
        return new BitwiseShiftGenerator(
            op,
            operands.get(0).getType(),
            forPrimitive(operands.get(0)),
            forPrimitive(operands.get(1)));

      case CONVERT_EXPR:
      case FIX_TRUNC_EXPR:
      case FLOAT_EXPR:
      case PAREN_EXPR:
      case VAR_DECL:
      case PARM_DECL:
      case NOP_EXPR:
      case MEM_REF:
      case INTEGER_CST:
      case REAL_CST:
      case STRING_CST:
      case COMPLEX_CST:
      case ADDR_EXPR:
      case ARRAY_REF:
      case COMPONENT_REF:
      case REALPART_EXPR:
      case IMAGPART_EXPR:
        return forPrimitive(operands.get(0));

      case NEGATE_EXPR:
        return new NegateGenerator(forPrimitive(operands.get(0)));

      case TRUTH_NOT_EXPR:
        return new LogicalNotGenerator(forPrimitive(operands.get(0)));

      case TRUTH_AND_EXPR:
        return new LogicalAndGenerator(
            forPrimitive(operands.get(0)),
            forPrimitive(operands.get(1)));

      case TRUTH_OR_EXPR:
        return new LogicalOrGenerator(
            forPrimitive(operands.get(0)),
            forPrimitive(operands.get(1)));

      case TRUTH_XOR_EXPR:
        return new LogicalXorGenerator(
            forPrimitive(operands.get(0)),
            forPrimitive(operands.get(1)));

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
      case MIN_EXPR:
        return new MinMaxGenerator(op,
            forPrimitive(operands.get(0)),
            forPrimitive(operands.get(1)));

      case ABS_EXPR:
        return new AbsGenerator(
            forPrimitive(operands.get(0)));

      case UNORDERED_EXPR:
        return new UnorderedExprGenerator(
            forPrimitive(operands.get(0)),
            forPrimitive(operands.get(1)));
      
      
      default:
        throw new UnsupportedOperationException("op: " + op);
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
        return findGenerator(operands.get(0))
            .pointerPlus(
                findGenerator(operands.get(1)));

      case BIT_NOT_EXPR:
        return new BitwiseNotGenerator(findGenerator(operands.get(0)));

      case LSHIFT_EXPR:
      case RSHIFT_EXPR:
        return new BitwiseShiftGenerator(
            op,
            operands.get(0).getType(),
            findGenerator(operands.get(0)),
            findGenerator(operands.get(1)));

      case CONVERT_EXPR:
      case FIX_TRUNC_EXPR:
      case FLOAT_EXPR:
      case PAREN_EXPR:
      case VAR_DECL:
      case PARM_DECL:
      case NOP_EXPR:
      case MEM_REF:
      case INTEGER_CST:
      case REAL_CST:
      case STRING_CST:
      case COMPLEX_CST:
      case ADDR_EXPR:
      case ARRAY_REF:
      case COMPONENT_REF:
      case REALPART_EXPR:
      case IMAGPART_EXPR:
        return findGenerator(operands.get(0));

      case COMPLEX_EXPR:
        return new ComplexConstructorGenerator(findGenerator(operands.get(0)));

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
      
      case TRUTH_XOR_EXPR:
        return new LogicalXorGenerator(
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
      case MIN_EXPR:
        return new MinMaxGenerator(op,
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

  public static ExprGenerator forConstant(GimpleConstant constant) {
    if (constant.isNull()) {
      return new NullPtrGenerator(constant.getType());
    } else if (constant instanceof GimplePrimitiveConstant) {
      return new PrimitiveConstGenerator((GimplePrimitiveConstant) constant);
    } else if (constant instanceof GimpleComplexConstant) {
      GimpleComplexConstant complexConstant = (GimpleComplexConstant) constant;
      return new ComplexValue(complexConstant)
    } else if (constant instanceof GimpleStringConstant) {
      return new StringConstantGenerator(constant);
    } else {
      throw new UnsupportedOperationException("constant: " + constant);
    }
  }


  public Optional<ExprGenerator> findGenerator(Optional<GimpleExpr> expr) {
    if(expr.isPresent()) {
      return Optional.of(findGenerator(expr.get()));
    } else {
      return Optional.absent();
    }
  }
}
