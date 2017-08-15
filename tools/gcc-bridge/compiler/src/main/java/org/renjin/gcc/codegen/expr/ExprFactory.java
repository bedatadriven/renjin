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
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayExpr;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunPtrCallGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.ConstConditionGenerator;
import org.renjin.gcc.codegen.condition.NullCheckGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.complex.ComplexCmpGenerator;
import org.renjin.gcc.codegen.type.complex.ComplexValue;
import org.renjin.gcc.codegen.type.complex.ComplexValues;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.*;
import org.renjin.gcc.codegen.type.primitive.op.*;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.symbols.SymbolTable;
import org.renjin.repackaged.asm.Type;

import java.util.List;


/**
 * Creates code-generating {@link GExpr}s from {@code GimpleExpr}s
 */
public class ExprFactory {
  private final TypeOracle typeOracle;
  private final SymbolTable symbolTable;
  private MethodGenerator mv;

  public ExprFactory(TypeOracle typeOracle, SymbolTable symbolTable, MethodGenerator mv) {
    this.typeOracle = typeOracle;
    this.symbolTable = symbolTable;
    this.mv = mv;
  }
  

  public GExpr findGenerator(GimpleExpr expr, GimpleType expectedType) {
    return maybeCast(findGenerator(expr), expectedType, expr.getType());
  }

  public GExpr maybeCast(GExpr rhs, GimpleType lhsType, GimpleType rhsType) {
    
    if(lhsType.equals(rhsType)) {
      return rhs;
    }
    
    TypeStrategy leftStrategy = typeOracle.forType(lhsType);
    TypeStrategy rightStrategy = typeOracle.forType(rhsType);

    if(ConstantValue.isZero(rhs) && leftStrategy instanceof PointerTypeStrategy) {
      return ((PointerTypeStrategy) leftStrategy).nullPointer();
    }
    
    try {
      return leftStrategy.cast(mv, rhs, rightStrategy);
    } catch (UnsupportedCastException e) {
      throw new InternalCompilerException(String.format("Unsupported cast to %s [%s] from %s [%s]",
          lhsType, leftStrategy.getClass().getSimpleName(),
          rhsType, rightStrategy.getClass().getSimpleName()), e);
    }
  }

  public GExpr findGenerator(GimpleExpr expr) {
    if(expr instanceof GimpleSymbolRef) {
      GExpr variable = symbolTable.getVariable((GimpleSymbolRef) expr);
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
        return new FunPtr(symbolTable.findHandle(functionRef));

      } else if(addressOf.getValue() instanceof GimplePrimitiveConstant) {
        // Exceptionally, gimple often contains to address of constants when
        // passing them to functions

        JExpr value = findPrimitiveGenerator(addressOf.getValue());
        return new FatPtrPair(new PrimitiveValueFunction(value.getType()), Expressions.newArray(value));

      } else  {

        // Try to simplify expressions in the form &*x to x
        if(addressOf.getValue() instanceof GimpleMemRef) {
          GimpleMemRef memRef = (GimpleMemRef) addressOf.getValue();
          if(memRef.isOffsetZero()) {
            return findGenerator(memRef.getPointer());
          } else {
            return pointerPlus(memRef.getPointer(), memRef.getOffset(), memRef.getPointer().getType());
          }
        }

        // Otherwise delgate addressOf operation to expr generator
        GExpr value = findGenerator(addressOf.getValue());
        try {
          return value.addressOf();
        } catch (ClassCastException | UnsupportedOperationException ignored) {
          throw new InternalCompilerException(addressOf.getValue() + " [" + value.getClass().getName() + "] is not addressable");
        }
      }

    } else if(expr instanceof GimpleMemRef) {
      GimpleMemRef memRefExpr = (GimpleMemRef) expr;
      return memRef(memRefExpr, memRefExpr.getType());

    } else if(expr instanceof GimpleArrayRef) {
      GimpleArrayRef arrayRef = (GimpleArrayRef) expr;
      ArrayTypeStrategy arrayStrategy = typeOracle.forArrayType(arrayRef.getArray().getType());
      GExpr array = findGenerator(arrayRef.getArray());
      GExpr index = findGenerator(arrayRef.getIndex());
      
      return arrayStrategy.elementAt(array, index);
      
    } else if(expr instanceof GimpleConstantRef) {
      GimpleConstant constant = ((GimpleConstantRef) expr).getValue();
      JExpr constantValue = findPrimitiveGenerator(constant);
      FatPtrPair address = new FatPtrPair(
          new PrimitiveValueFunction(constantValue.getType()), 
          Expressions.newArray(constantValue));
      
      return new PrimitiveValue(constantValue, address);

    } else if(expr instanceof GimpleComplexPartExpr) {
      GimpleExpr complexExpr = ((GimpleComplexPartExpr) expr).getComplexValue();
      ComplexValue complexGenerator = (ComplexValue) findGenerator(complexExpr);
      if (expr instanceof GimpleRealPartExpr) {
        return complexGenerator.getRealGExpr();
      } else {
        return complexGenerator.getImaginaryGExpr();
      }
    } else if (expr instanceof GimpleComponentRef) {
      GimpleComponentRef ref = (GimpleComponentRef) expr;
      GExpr instance = findGenerator(((GimpleComponentRef) expr).getValue());
      RecordTypeStrategy typeStrategy = (RecordTypeStrategy) typeOracle.forType(ref.getValue().getType());
      TypeStrategy fieldTypeStrategy = typeOracle.forType(ref.getType());
      return typeStrategy.memberOf(mv, instance,
          ref.getMember().getOffset(), 
          ref.getMember().getSize(), 
          fieldTypeStrategy);

    } else if (expr instanceof GimpleBitFieldRefExpr) {
      GimpleBitFieldRefExpr ref = (GimpleBitFieldRefExpr) expr;
      GExpr instance = findGenerator(ref.getValue());
      RecordTypeStrategy recordTypeStrategy = (RecordTypeStrategy) typeOracle.forType(ref.getValue().getType());
      TypeStrategy memberTypeStrategy = typeOracle.forType(expr.getType());

      return recordTypeStrategy.memberOf(mv, instance, ref.getOffset(), ref.getSize(), memberTypeStrategy);
      
      
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

  private GExpr forConstructor(GimpleConstructor expr) {
    return typeOracle.forType(expr.getType()).constructorExpr(this, mv, expr);
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
    FunPtr expr = (FunPtr) findGenerator(functionExpr);
    return new FunPtrCallGenerator(typeOracle, (GimpleFunctionType) functionExpr.getType().getBaseType(), expr.unwrap());
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
      if(x.getType() instanceof GimpleIntegerType && ((GimpleIntegerType) x.getType()).isUnsigned()) {
        return PrimitiveCmpGenerator.unsigned(op, findPrimitiveGenerator(x), findPrimitiveGenerator(y));
      } else {
        return new PrimitiveCmpGenerator(op, findPrimitiveGenerator(x), findPrimitiveGenerator(y));
      }

    } else if(x.getType() instanceof GimpleIndirectType) {
      
      return comparePointers(op, x, y);
      
    } else {
      throw new UnsupportedOperationException("Unsupported comparison " + op + " between types " +
          x.getType() + " and " + y.getType());
    }
  }

  private ConditionGenerator comparePointers(GimpleOp op, GimpleExpr x, GimpleExpr y) {
    
    // First see if this is a null check
    if(isNull(x) && isNull(y)) {
      switch (op) {
        case EQ_EXPR:
        case GE_EXPR:
        case LE_EXPR:
          return new ConstConditionGenerator(true);
        case NE_EXPR:
        case LT_EXPR:
        case GT_EXPR:
          return new ConstConditionGenerator(false);
        default:
          throw new UnsupportedOperationException("op: " + op);
      }
    } else if(isNull(x)) {
      return new NullCheckGenerator(op, (PtrExpr) findGenerator(y));
    } else if(isNull(y)) {
      return new NullCheckGenerator(op, (PtrExpr) findGenerator(x));
    }
    
    // Shouldn't matter which we pointer we cast to the other, but if we have a choice,
    // cast away from a void* to a concrete pointer type
    GimpleType commonType;

    if(x.getType().isPointerTo(GimpleVoidType.class)) {
      commonType = y.getType();
    } else {
      commonType = x.getType();
    }

    PointerTypeStrategy typeStrategy = typeOracle.forPointerType(commonType);
    GExpr ptrX = findGenerator(x, commonType);
    GExpr ptrY = findGenerator(y, commonType);

    return typeStrategy.comparePointers(mv, op, ptrX, ptrY);
  }

  private boolean isNull(GimpleExpr expr) {
    return expr instanceof GimpleConstant && ((GimpleConstant) expr).isNull();
  }

  public GExpr findGenerator(GimpleOp op, List<GimpleExpr> operands, GimpleType expectedType) {
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
        return primitive(new BitwiseNot(findPrimitiveGenerator(operands.get(0))));

      case LSHIFT_EXPR:
      case RSHIFT_EXPR:
        return primitive(new BitwiseShift(
            op,
            operands.get(0).getType(),
            findPrimitiveGenerator(operands.get(0)),
            findPrimitiveGenerator(operands.get(1))));

      case MEM_REF:
        // Cast the pointer type first, then dereference
        return memRef((GimpleMemRef) operands.get(0), expectedType);

      case CONSTRUCTOR:
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
      case BIT_FIELD_REF:
      case REALPART_EXPR:
      case IMAGPART_EXPR:
        return maybeCast(findGenerator(operands.get(0)), expectedType, operands.get(0).getType());
      
      case COMPLEX_EXPR:
        return new ComplexValue(findPrimitiveGenerator(operands.get(0)));

      case NEGATE_EXPR:
        return primitive(new NegativeValue(findPrimitiveGenerator(operands.get(0))));

      case TRUTH_NOT_EXPR:
        return primitive(new LogicalNot(findPrimitiveGenerator(operands.get(0))));

      case TRUTH_AND_EXPR:
        return primitive(new LogicalAnd(
            findPrimitiveGenerator(operands.get(0)),
            findPrimitiveGenerator(operands.get(1))));

      case TRUTH_OR_EXPR:
        return primitive(new LogicalOr(
            findPrimitiveGenerator(operands.get(0)),
            findPrimitiveGenerator(operands.get(1))));
      
      case TRUTH_XOR_EXPR:
        return primitive(new LogicalXor(
            findPrimitiveGenerator(operands.get(0)),
            findPrimitiveGenerator(operands.get(1))));

      case EQ_EXPR:
      case LT_EXPR:
      case LE_EXPR:
      case NE_EXPR:
      case GT_EXPR:
      case GE_EXPR:
      case UNORDERED_EXPR:
      case ORDERED_EXPR:
      case UNEQ_EXPR:
      case UNLT_EXPR:
      case UNLE_EXPR:
      case UNGT_EXPR:
      case UNGE_EXPR:
        return primitive(new ConditionExpr(
            findComparisonGenerator(op,operands.get(0), operands.get(1))));

      case MAX_EXPR:
      case MIN_EXPR:
        return primitive(new MinMaxValue(op,
            findPrimitiveGenerator(operands.get(0)),
            findPrimitiveGenerator(operands.get(1))));

      case ABS_EXPR:
        return primitive(new AbsValue(
            findPrimitiveGenerator(operands.get(0))));

      case CONJ_EXPR:
        return findComplexGenerator(operands.get(0)).conjugate();


      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }

  private PrimitiveValue primitive(JExpr expr) {
    return new PrimitiveValue(expr);
  }

  private GExpr memRef(GimpleMemRef gimpleExpr, GimpleType expectedType) {
    GimpleExpr pointer = gimpleExpr.getPointer();
    
    // Case of *&x, which can be simplified to x
    if(pointer instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) pointer;
      return findGenerator(addressOf.getValue(), expectedType);
    }
    
    GimpleIndirectType pointerType = (GimpleIndirectType) pointer.getType();
    
    if(pointerType.getBaseType() instanceof GimpleVoidType) {
      // We can't dereference a null pointer, so cast the pointer first, THEN dereference
      return castThenDereference(gimpleExpr, expectedType);
    
    } else {
      return dereferenceThenCast(gimpleExpr, expectedType);
    }
  }

  private GExpr castThenDereference(GimpleMemRef gimpleExpr, GimpleType expectedType) {
    GimpleExpr pointer = gimpleExpr.getPointer();
    GimpleIndirectType pointerType = (GimpleIndirectType) pointer.getType();
    GimpleIndirectType expectedPointerType = expectedType.pointerTo();
    
    // Cast from the void pointer type to the "expected" pointer type
    GExpr ptrExpr = maybeCast(findGenerator(pointer), expectedPointerType, pointerType);
    PointerTypeStrategy pointerStrategy = typeOracle.forPointerType(expectedPointerType);

    if(!gimpleExpr.isOffsetZero()) {
      JExpr offsetInBytes = findPrimitiveGenerator(gimpleExpr.getOffset());

      ptrExpr =  pointerStrategy.pointerPlus(mv, ptrExpr, offsetInBytes);
    }

    return ((PtrExpr) ptrExpr).valueOf(expectedType);
  }
  
  private GExpr dereferenceThenCast(GimpleMemRef gimpleExpr, GimpleType expectedType) {
    GimpleExpr pointer = gimpleExpr.getPointer();
    GimpleIndirectType pointerType = (GimpleIndirectType) pointer.getType();
    PointerTypeStrategy pointerStrategy = typeOracle.forPointerType(pointerType);

    GExpr ptrExpr = findGenerator(pointer);

    if(!gimpleExpr.isOffsetZero()) {
      JExpr offsetInBytes = findPrimitiveGenerator(gimpleExpr.getOffset());
      ptrExpr =  pointerStrategy.pointerPlus(mv, ptrExpr, offsetInBytes);
    }
    
    GExpr valueExpr = ((PtrExpr) ptrExpr).valueOf(expectedType);

    return maybeCast(valueExpr, expectedType, pointerType.getBaseType());
  }

  private GExpr pointerPlus(GimpleExpr pointerExpr, GimpleExpr offsetExpr, GimpleType expectedType) {
    GExpr pointer = findGenerator(pointerExpr);
    JExpr offsetInBytes = findPrimitiveGenerator(offsetExpr);

    GimpleType pointerType = pointerExpr.getType();
    GExpr result = typeOracle.forPointerType(pointerType).pointerPlus(mv, pointer, offsetInBytes);
    
    return maybeCast(result, expectedType, pointerType);
  }

  private <T extends GExpr> T findGenerator(GimpleExpr gimpleExpr, Class<T> exprClass) {
    GExpr expr = findGenerator(gimpleExpr);
    if(exprClass.isAssignableFrom(expr.getClass())) {
      return exprClass.cast(expr);
    } else {
      throw new InternalCompilerException(String.format("Expected %s for expr %s, found: %s", 
          exprClass.getSimpleName(), 
          gimpleExpr, 
          expr.getClass().getName()));
    }
  }

  public JExpr findPrimitiveGenerator(GimpleExpr gimpleExpr) {
    // When looking specifically for a value generator, treat a null pointer as zero integer
    if(gimpleExpr instanceof GimplePrimitiveConstant && gimpleExpr.getType() instanceof GimpleIndirectType) {
      return Expressions.constantInt(((GimplePrimitiveConstant) gimpleExpr).getValue().intValue());
    }
    PrimitiveValue primitive = findGenerator(gimpleExpr, PrimitiveValue.class);
    return primitive.getExpr();
  }


  private ComplexValue findComplexGenerator(GimpleExpr gimpleExpr) {
    return findGenerator(gimpleExpr, ComplexValue.class);
  }
  
  private GExpr findBinOpGenerator(GimpleOp op, List<GimpleExpr> operands) {
    GimpleExpr x = operands.get(0);
    GimpleExpr y = operands.get(1);


    if( x.getType() instanceof GimpleComplexType && 
        y.getType() instanceof GimpleComplexType) {

      return complexBinOp(op, findComplexGenerator(x), findComplexGenerator(y));
      
    } else if(
        x.getType() instanceof GimplePrimitiveType &&
        y.getType() instanceof GimplePrimitiveType) {


      // Unsigned integer division needs to be handled specially on the JVM
      if((isUnsignedInt(x.getType()) || isUnsignedInt(y.getType())) &&
          isDivisionOperator(op)) {

        GimpleIntegerType dividendType = (GimpleIntegerType) x.getType();
        GimpleIntegerType divisorType = (GimpleIntegerType) y.getType();

        if(!dividendType.equals(divisorType)) {
          throw new UnsupportedOperationException("TODO: " + dividendType + " / " + divisorType);
        }

        switch (dividendType.getSize()) {
          case 32:
            return primitive(new UnsignedIntDiv(findPrimitiveGenerator(x), findPrimitiveGenerator(y)));
          default:
            throw new UnsupportedOperationException("unsigned integer division, size = " + dividendType.getSize());
        }
      }

      // Otherwise we can use builtin JVM operators

      return primitive(new PrimitiveBinOpGenerator(op, findPrimitiveGenerator(x), findPrimitiveGenerator(y)));

    }

    throw new UnsupportedOperationException(op.name() + ": " + x.getType() + ", " + y.getType());
  }

  private boolean isDivisionOperator(GimpleOp op) {
    switch (op) {
      case RDIV_EXPR:
      case TRUNC_DIV_EXPR:
      case EXACT_DIV_EXPR:
        return true;

      default:
        return false;
    }
  }

  private boolean isUnsignedInt(GimpleType type) {
    if(type instanceof GimpleIntegerType) {
      GimpleIntegerType integerType = (GimpleIntegerType) type;
      return integerType.isUnsigned();
    }
    return false;
  }

  private GExpr complexBinOp(GimpleOp op, ComplexValue cx, ComplexValue cy) {
    switch (op) {
      case PLUS_EXPR:
        return ComplexValues.add(cx, cy);
      case MINUS_EXPR:
        return ComplexValues.subtract(cx, cy);
      case MULT_EXPR:
        return ComplexValues.multiply(cx, cy);
      default:
        throw new UnsupportedOperationException("complex operation: " + op);
    }
  }

  public GExpr forConstant(GimpleConstant constant) {
    if (constant.getType() instanceof GimpleIndirectType) {
      // TODO: Treat all pointer constants as null
      return typeOracle.forPointerType(constant.getType()).nullPointer();
      
    } else if (constant instanceof GimplePrimitiveConstant) {
      return primitive(new ConstantValue((GimplePrimitiveConstant) constant));
      
    } else if (constant instanceof GimpleComplexConstant) {
      GimpleComplexConstant complexConstant = (GimpleComplexConstant) constant;
      return new ComplexValue(
          forConstant(complexConstant.getReal()), 
          forConstant(complexConstant.getIm()));
      
    } else if (constant instanceof GimpleStringConstant) {
      StringConstant array = new StringConstant(((GimpleStringConstant) constant).getValue());
      ArrayExpr arrayExpr = new ArrayExpr(new PrimitiveValueFunction(Type.BYTE_TYPE), array.getLength(), array);
      return arrayExpr;
      
    } else {
      throw new UnsupportedOperationException("constant: " + constant);
    }
  }


  public TypeStrategy strategyFor(GimpleType type) {
    return typeOracle.forType(type);
  }
}
