package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunPtrCallGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.complex.ComplexCmpGenerator;
import org.renjin.gcc.codegen.type.complex.ComplexValue;
import org.renjin.gcc.codegen.type.complex.ComplexValues;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.primitive.PrimitiveCmpGenerator;
import org.renjin.gcc.codegen.type.primitive.StringConstant;
import org.renjin.gcc.codegen.type.primitive.op.*;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.symbols.SymbolTable;

import java.util.List;


/**
 * Creates code-generating {@link Expr}s from {@code GimpleExpr}s
 */
public class ExprFactory {
  private final TypeOracle typeOracle;
  private final SymbolTable symbolTable;

  public ExprFactory(TypeOracle typeOracle, SymbolTable symbolTable) {
    this.typeOracle = typeOracle;
    this.symbolTable = symbolTable;
  }
  

  public Expr findGenerator(GimpleExpr expr, GimpleType expectedType) {
    return maybeCast(findGenerator(expr), expectedType, expr.getType());
  }

  public Expr maybeCast(Expr rhs, GimpleType lhsType, GimpleType rhsType) {
    
    if(lhsType.equals(rhsType)) {
      return rhs;
    }
    
    TypeStrategy leftStrategy = typeOracle.forType(lhsType);
    TypeStrategy rightStrategy = typeOracle.forType(rhsType);

    if(ConstantValue.isZero(rhs) && leftStrategy instanceof PointerTypeStrategy) {
      return ((PointerTypeStrategy) leftStrategy).nullPointer();
    }
    
    try {
      return leftStrategy.cast(rhs, rightStrategy);
    } catch (UnsupportedCastException e) {
      throw new InternalCompilerException(String.format("Unsupported cast to %s [%s] from %s [%s]",
          lhsType, leftStrategy.getClass().getSimpleName(),
          rhsType, rightStrategy.getClass().getSimpleName()), e);
    }
  }

  public Expr findGenerator(GimpleExpr expr) {
    if(expr instanceof GimpleSymbolRef) {
      Expr variable = symbolTable.getVariable((GimpleSymbolRef) expr);
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
        return symbolTable.findHandle(functionRef);

      } else if(addressOf.getValue() instanceof GimplePrimitiveConstant) {
        // Exceptionally, gimple often contains to address of constants when
        // passing them to functions

        SimpleExpr value = findValueGenerator(addressOf.getValue());
        return new FatPtrExpr(Expressions.newArray(value));

      } else  {
        Expr value = findGenerator(addressOf.getValue());
        try {
          return ((Addressable) value).addressOf();
        } catch (ClassCastException | UnsupportedOperationException ignored) {
          throw new InternalCompilerException(addressOf.getValue() + " [" + value.getClass().getName() + "] is not addressable");
        }
      }

    } else if(expr instanceof GimpleMemRef) {
      GimpleMemRef memRefExpr = (GimpleMemRef) expr;
      PointerTypeStrategy typeStrategy = typeOracle.forPointerType(memRefExpr.getPointer().getType());
      Expr ptrExpr = findGenerator(memRefExpr.getPointer());

      if(!memRefExpr.isOffsetZero()) {
        SimpleExpr offsetInBytes = findValueGenerator(memRefExpr.getOffset());
        ptrExpr =  typeStrategy.pointerPlus(ptrExpr, offsetInBytes);
      }
      
      return typeStrategy.valueOf(ptrExpr);
      
    } else if(expr instanceof GimpleArrayRef) {
      GimpleArrayRef arrayRef = (GimpleArrayRef) expr;
      ArrayTypeStrategy arrayStrategy = typeOracle.forArrayType(arrayRef.getArray().getType());
      Expr array = findGenerator(arrayRef.getArray());
      Expr index = findGenerator(arrayRef.getIndex());
      
      return arrayStrategy.elementAt(array, index);
      
    } else if(expr instanceof GimpleConstantRef) {
      GimpleConstant constant = ((GimpleConstantRef) expr).getValue();
      SimpleExpr constantValue = findValueGenerator(constant);
      FatPtrExpr address = new FatPtrExpr(Expressions.newArray(constantValue));
      
      return new SimpleAddressableExpr(constantValue, address);

    } else if(expr instanceof GimpleComplexPartExpr) {
      GimpleExpr complexExpr = ((GimpleComplexPartExpr) expr).getComplexValue();
      ComplexValue complexGenerator = (ComplexValue) findGenerator(complexExpr);
      if (expr instanceof GimpleRealPartExpr) {
        return complexGenerator.getRealValue();
      } else {
        return complexGenerator.getImaginaryValue();
      }
    } else if (expr instanceof GimpleComponentRef) {
      GimpleComponentRef ref = (GimpleComponentRef) expr;
      Expr instance = findGenerator(((GimpleComponentRef) expr).getValue());
      RecordTypeStrategy typeStrategy = (RecordTypeStrategy) typeOracle.forType(ref.getValue().getType());
      return typeStrategy.memberOf(instance, (GimpleFieldRef) ref.getMember());
   
    } else if(expr instanceof GimpleCompoundLiteral) {
      return findGenerator(((GimpleCompoundLiteral) expr).getDecl());
    
    } else if(expr instanceof GimpleObjectTypeRef) {
      GimpleObjectTypeRef typeRef = (GimpleObjectTypeRef) expr;
      
      return findGenerator(typeRef.getExpr());
    
    } else if(expr instanceof GimplePointerPlus) {
      GimplePointerPlus pointerPlus = (GimplePointerPlus) expr;
      return pointerPlus(pointerPlus.getPointer(), pointerPlus.getOffset(), pointerPlus.getType());
    }
    
    throw new UnsupportedOperationException(expr + " [" + expr.getClass().getSimpleName() + "]");
  }

  private Expr forConstructor(GimpleConstructor expr) {
    return typeOracle.forType(expr.getType()).constructorExpr(this, expr);
  }

  public CallGenerator findCallGenerator(GimpleExpr functionExpr) {
    if(functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return symbolTable.findCallGenerator(ref);
      }
      GimpleAddressOf address = (GimpleAddressOf) functionExpr;
      throw new UnsupportedOperationException("function ref: " + address.getValue() +
          " [" + address.getValue().getClass().getSimpleName() + "]");

    }

    // Assume this is a function pointer ptr expression  
    Expr expr = findGenerator(functionExpr);
    return new FunPtrCallGenerator(typeOracle, (GimpleFunctionType) functionExpr.getType().getBaseType(), 
        (SimpleExpr) expr);
  }

  public ConditionGenerator findConditionGenerator(GimpleOp op, List<GimpleExpr> operands) {
    if(operands.size() == 2) {
      return findComparisonGenerator(op, operands.get(0), operands.get(1));
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private ConditionGenerator findComparisonGenerator(GimpleOp op, GimpleExpr x, GimpleExpr y) {

    if(x.getType() instanceof org.renjin.gcc.gimple.type.GimpleComplexType) {
      return new ComplexCmpGenerator(op, findComplexGenerator(x), findComplexGenerator(y));

    } else if(x.getType() instanceof GimplePrimitiveType) {
      return new PrimitiveCmpGenerator(op, findValueGenerator(x), findValueGenerator(y));

    } else if(x.getType() instanceof GimpleIndirectType) {
      
      return comparePointers(op, x, y);
      
    } else {
      throw new UnsupportedOperationException("Unsupported comparison " + op + " between types " +
          x.getType() + " and " + y.getType());
    }
  }

  private ConditionGenerator comparePointers(GimpleOp op, GimpleExpr x, GimpleExpr y) {
    
    Expr ptrX = findGenerator(x);
    Expr ptrY = findGenerator(y);
    
    // Shoudldn't matter which we pointer we cast to the other, but if we have a choice,
    // cast away from a void* to a concrete pointer type
    if(x.getType().isPointerTo(GimpleVoidType.class)) {
      ptrY = maybeCast(ptrY, x.getType(), y.getType());
    } else {
      ptrX = maybeCast(ptrX, y.getType(), x.getType());
    }

    return typeOracle.forPointerType(x.getType()).comparePointers(op, ptrX, ptrY);
  }

  public Expr findGenerator(GimpleOp op, List<GimpleExpr> operands, GimpleType expectedType) {
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
        return pointerPlus(operands.get(0), operands.get(1), expectedType);

      case BIT_NOT_EXPR:
        return new BitwiseNot((SimpleExpr)findGenerator(operands.get(0)));

      case LSHIFT_EXPR:
      case RSHIFT_EXPR:
        return new BitwiseShift(
            op,
            operands.get(0).getType(),
            (SimpleExpr)findGenerator(operands.get(0)),
            (SimpleExpr)findGenerator(operands.get(1)));

      case MEM_REF:
        // Cast the pointer type first, then dereference
        return memRef((GimpleMemRef) operands.get(0), expectedType);
      
      case CONVERT_EXPR:
      case FIX_TRUNC_EXPR:
      case FLOAT_EXPR:
      case PAREN_EXPR:
      case VAR_DECL:
      case PARM_DECL:
      case NOP_EXPR:
      case INTEGER_CST:
      case REAL_CST:
      case STRING_CST:
      case COMPLEX_CST:
      case ADDR_EXPR:
      case ARRAY_REF:
      case COMPONENT_REF:
      case REALPART_EXPR:
      case IMAGPART_EXPR:
        return maybeCast(findGenerator(operands.get(0)), expectedType, operands.get(0).getType());
      
      case COMPLEX_EXPR:
        return new ComplexValue(findValueGenerator(operands.get(0)));

      case NEGATE_EXPR:
        return new NegativeValue(findValueGenerator(operands.get(0)));

      case TRUTH_NOT_EXPR:
        return new LogicalNot(findValueGenerator(operands.get(0)));

      case TRUTH_AND_EXPR:
        return new LogicalAnd(
            findValueGenerator(operands.get(0)),
            findValueGenerator(operands.get(1)));

      case TRUTH_OR_EXPR:
        return new LogicalOr(
            findValueGenerator(operands.get(0)),
            findValueGenerator(operands.get(1)));
      
      case TRUTH_XOR_EXPR:
        return new LogicalXor(
            findValueGenerator(operands.get(0)),
            findValueGenerator(operands.get(1)));

      case EQ_EXPR:
      case LT_EXPR:
      case LE_EXPR:
      case NE_EXPR:
      case GT_EXPR:
      case GE_EXPR:
        return new ConditionExpr(
            findComparisonGenerator(op,operands.get(0), operands.get(1)));

      case MAX_EXPR:
      case MIN_EXPR:
        return new MinMaxValue(op,
            findValueGenerator(operands.get(0)),
            findValueGenerator(operands.get(1)));

      case ABS_EXPR:
        return new AbsValue(
            findValueGenerator(operands.get(0)));

      case UNORDERED_EXPR:
        return new UnorderedExpr(
            findValueGenerator(operands.get(0)),
            findValueGenerator(operands.get(1)));

      case CONJ_EXPR:
        return findComplexGenerator(operands.get(0)).conjugate();

      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }



  private Expr memRef(GimpleMemRef gimpleExpr, GimpleType expectedType) {
    GimpleExpr pointer = gimpleExpr.getPointer();
    GimpleIndirectType pointerType = (GimpleIndirectType) pointer.getType();
    GimpleIndirectType expectedPointerType = expectedType.pointerTo();
    
    Expr ptrExpr = maybeCast(findGenerator(pointer), expectedPointerType, pointerType);

    PointerTypeStrategy pointerStrategy = typeOracle.forPointerType(expectedPointerType);

    if(!gimpleExpr.isOffsetZero()) {
      SimpleExpr offsetInBytes = findValueGenerator(gimpleExpr.getOffset());

      ptrExpr =  pointerStrategy.pointerPlus(ptrExpr, offsetInBytes);
    }

    return pointerStrategy.valueOf(ptrExpr);
  }

  private Expr pointerPlus(GimpleExpr pointerExpr, GimpleExpr offsetExpr, GimpleType expectedType) {
    Expr pointer = findGenerator(pointerExpr);
    SimpleExpr offsetInBytes = findValueGenerator(offsetExpr);

    GimpleType pointerType = pointerExpr.getType();
    Expr result = typeOracle.forPointerType(pointerType).pointerPlus(pointer, offsetInBytes);
    
    return maybeCast(result, expectedType, pointerType);
  }

  private <T extends Expr> T findGenerator(GimpleExpr gimpleExpr, Class<T> exprClass) {
    Expr expr = findGenerator(gimpleExpr);
    if(exprClass.isAssignableFrom(expr.getClass())) {
      return exprClass.cast(expr);
    } else {
      throw new InternalCompilerException(String.format("Expected %s for expr %s, found: %s", 
          exprClass.getSimpleName(), 
          gimpleExpr, 
          expr.getClass().getName()));
    }
  }

  public SimpleExpr findValueGenerator(GimpleExpr gimpleExpr) {
    // When looking specifically for a value generator, treat a null pointer as zero integer
    if(gimpleExpr instanceof GimplePrimitiveConstant && gimpleExpr.getType() instanceof GimpleIndirectType) {
      return Expressions.constantInt(((GimplePrimitiveConstant) gimpleExpr).getValue().intValue());
    }
    return findGenerator(gimpleExpr, SimpleExpr.class);
  }


  private ComplexValue findComplexGenerator(GimpleExpr gimpleExpr) {
    return findGenerator(gimpleExpr, ComplexValue.class);
  }
  
  private Expr findBinOpGenerator(GimpleOp op, List<GimpleExpr> operands) {
    GimpleExpr x = operands.get(0);
    GimpleExpr y = operands.get(1);
    

    if( x.getType() instanceof GimpleComplexType && 
        y.getType() instanceof GimpleComplexType) {

      return complexBinOp(op, findComplexGenerator(x), findComplexGenerator(y));
      
    } else if(
        x.getType() instanceof GimplePrimitiveType &&
        y.getType() instanceof GimplePrimitiveType) {

      return new PrimitiveBinOpGenerator(op, findValueGenerator(x), findValueGenerator(y));

    }

    throw new UnsupportedOperationException(op.name() + ": " + x.getType() + ", " + y.getType());
  }

  private Expr complexBinOp(GimpleOp op, ComplexValue cx, ComplexValue cy) {
    switch (op) {
      case PLUS_EXPR:
        return ComplexValues.add(cx, cy);
      case MINUS_EXPR:
        return ComplexValues.subtract(cx, cy);
      case MULT_EXPR:
        return ComplexValues.multiply(cx, cy);
      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }

  public Expr forConstant(GimpleConstant constant) {
    if (constant.getType() instanceof GimpleIndirectType) {
      // TODO: Treat all pointer constants as null
      return typeOracle.forPointerType(constant.getType()).nullPointer();
      
    } else if (constant instanceof GimplePrimitiveConstant) {
      return new ConstantValue((GimplePrimitiveConstant) constant);
      
    } else if (constant instanceof GimpleComplexConstant) {
      GimpleComplexConstant complexConstant = (GimpleComplexConstant) constant;
      return new ComplexValue(
          (SimpleExpr)forConstant(complexConstant.getReal()), 
          (SimpleExpr)forConstant(complexConstant.getIm()));
      
    } else if (constant instanceof GimpleStringConstant) {
      StringConstant array = new StringConstant(((GimpleStringConstant) constant).getValue());
      FatPtrExpr address = new FatPtrExpr(array);
      FatPtrExpr arrayExpr = new FatPtrExpr(address, array, Expressions.zero());
      return arrayExpr;
      
    } else {
      throw new UnsupportedOperationException("constant: " + constant);
    }
  }


  public TypeStrategy strategyFor(GimpleType type) {
    return typeOracle.forType(type);
  }
}
